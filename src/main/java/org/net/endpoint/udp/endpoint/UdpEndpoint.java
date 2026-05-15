package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.jctools.queues.varhandle.MpscVarHandleArrayQueue;
import org.net.endpoint.Endpoint;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.NetService;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.maintenance.MaintenanceScheduler;
import org.net.endpoint.maintenance.task.BrokenConnectionCleanUpTask;
import org.net.endpoint.maintenance.task.HeartbeatSenderTask;
import org.net.endpoint.maintenance.task.IdleConnectionCleanUpTask;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.PendingConnection;
import org.net.endpoint.udp.connection.UdpConnection;
import org.net.endpoint.udp.sender.PacketSender;
import org.net.endpoint.udp.sender.PacketSenderMetrics;
import org.net.endpoint.udp.sender.SendRequest;
import org.net.endpoint.udp.sender.strategy.send.BatchSendStrategy;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.net.endpoint.udp.sender.PacketSender.DEFAULT_QUEUE_CAPACITY;

public abstract class UdpEndpoint extends NetService implements Endpoint {
	public static final int FRAMEWORK_CMD_SIZE = 64;
	public static final int SAFE_MTU_SIZE = 1200;

	private static final String NAME_POSTFIX = ".receiver";

	protected final TimeMachine timeMachine;
	protected final EndpointConfig config;
	protected final Supplier<UdpConnection> connSupplier;
	protected final Supplier<PendingConnection> pendingConnSupplier;
	protected final List<EndpointListener> listeners = new CopyOnWriteArrayList<>();
	protected int effectivePort = -1;
	protected final BufferPool bufferPool;

	protected InetSocketAddress address;
	protected DatagramChannel channel;
	protected ConnectionManager connMgr;
	protected PacketSender sender;
	protected UdpFramework udpFramework;
	protected MaintenanceScheduler maintenanceScheduler;
	protected ByteBuffer incomingBuffer;

	protected final UdpEndpointMetrics metrics = new UdpEndpointMetrics();

	public UdpEndpoint(
			@NonNull EndpointConfig config,
			@NonNull TimeMachine timeMachine,
			@NonNull Supplier<UdpConnection> udpConnectionSupplier,
			@NonNull Supplier<PendingConnection> pendingConnectionSupplier,
			@NonNull BufferPool bufferPool
	) {
		super(config.name() + NAME_POSTFIX, false);
		this.bufferPool = bufferPool;
		this.timeMachine = timeMachine;
		this.config = config;
		connSupplier = udpConnectionSupplier;
		this.pendingConnSupplier = pendingConnectionSupplier;
	}

	public UdpEndpointMetricsView metrics() {
		return metrics.view();
	}

	@Override
	public int effectivePort() {
		return effectivePort;
	}

	@Override
	public abstract void run();

	@Override
	protected void beforeStart() throws Exception {
		channel = DatagramChannel.open();
		address = new InetSocketAddress(config.host(), config.port());
		channel.bind(address);
		channel.configureBlocking(false);
		InetSocketAddress currentAddress = ((InetSocketAddress) channel.getLocalAddress());
		effectivePort = currentAddress.getPort();
		PacketSenderMetrics senderMetrics = new PacketSenderMetrics();
		sender = new PacketSender(
				config.name(),
				senderMetrics,
				new BatchSendStrategy(
						channel,
						timeMachine,
						senderMetrics,
						bufferPool
				),
				new MpscVarHandleArrayQueue<SendRequest>(DEFAULT_QUEUE_CAPACITY)
		);
		sender.start().await();
		udpFramework = new UdpFramework(bufferPool, sender);
		connMgr = new ConnectionManager(
				udpFramework,
				timeMachine,
				connSupplier,
				pendingConnSupplier,
				config.maxIncomingConnectionCount(),
				config.maxOutgoingConnectionCount()
		);
		maintenanceScheduler = createMaintenanceScheduler();
		maintenanceScheduler.start().await();
	}

	@Override
	protected void afterStop() throws Exception {
		sender.stop().await();
		maintenanceScheduler.stop().await();
		channel.close();
	}

	@Override
	public void registerListener(@NonNull EndpointListener listener) {
		listeners.add(listener);
	}

	private MaintenanceScheduler createMaintenanceScheduler() {
		MaintenanceScheduler scheduler = new MaintenanceScheduler(
				config.name(),
				config.maintenanceInterval(),
				timeMachine
		);
		if (config.heartbeatTimeout() > 0L) {
			scheduler.addTask(
					new BrokenConnectionCleanUpTask(connMgr, listeners, config.heartbeatTimeout())
			);
		}
		if (config.heartbeatInterval() > 0L) {
			scheduler.addTask(new HeartbeatSenderTask(connMgr, config.heartbeatInterval()));
		}
		if (config.idleTimeout() > 0L) {
			scheduler.addTask(new IdleConnectionCleanUpTask(connMgr, listeners, config.idleTimeout()));
		}
		return scheduler;
	}

	public void connect(@NonNull String host, int port) {
		if (isRunning()) {
			connMgr.createPendingOutgoingConnection(new InetSocketAddress(host, port));
		}
		else {
			throw new IllegalStateException("The endpoint must be started first!");
		}
	}

}
