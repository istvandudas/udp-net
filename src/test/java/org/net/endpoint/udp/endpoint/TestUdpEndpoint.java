package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.connection.PendingConnection;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public class TestUdpEndpoint extends UdpEndpoint {

	private final AtomicLong tick = new AtomicLong(0L);
	private final long parkTime;

	public TestUdpEndpoint(
			@NonNull EndpointConfig config,
			@NonNull TimeMachine timeMachine,
			@NonNull Supplier<UdpConnection> udpConnectionSupplier,
			@NonNull Supplier<PendingConnection> pendingConnSupplier,
			@NonNull BufferPool bufferPool,
			long parkTime
	) {
		super(config, timeMachine, udpConnectionSupplier, pendingConnSupplier, bufferPool);
		this.parkTime = parkTime;
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				tick.incrementAndGet();
				LockSupport.parkNanos(parkTime);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public long tick() {
		return tick.get();
	}

}
