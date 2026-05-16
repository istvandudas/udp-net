package org.net.endpoint.udp.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.Endpoint;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.connection.PendingConnection;
import org.net.endpoint.udp.connection.UnreliableUdpConnection;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class UnreliableUdpEndpoint extends UdpEndpoint implements Endpoint, Runnable {

	private static final long _100us = 100_000L;

	private final Map<UdpFrameworkMessage, FrameworkMessageHandler> messageHandlers =
			new EnumMap<>(UdpFrameworkMessage.class);

	public UnreliableUdpEndpoint(EndpointConfig config, TimeMachine timeMachine, BufferPool bufferPool) {
		super(config, timeMachine, UnreliableUdpConnection::create, PendingConnection::create, bufferPool);
		incomingBuffer = bufferPool.createForData();
		messageHandlers.put(UdpFrameworkMessage.CreateConnection, new UdpCreateConnectionHandler());
		messageHandlers.put(UdpFrameworkMessage.ConnectionAccepted, new UdpConnectionAcceptedHandler());
		messageHandlers.put(UdpFrameworkMessage.ConnectionRejected, new UdpConnectionRejectedHandler());
		messageHandlers.put(UdpFrameworkMessage.ConnectionClosed, new UdpConnectionClosedHandler());
		messageHandlers.put(UdpFrameworkMessage.Heartbeat, new UdpHeartbeatHandler());
		messageHandlers.put(UdpFrameworkMessage.Data, new UdpDataHandler());
	}

	@Override
	public void run() {
		HandlerContext context = new HandlerContext(config.name(), connMgr, metrics, listeners, incomingBuffer);
		try {
			while (running.get()) {
				InetSocketAddress sender;
				incomingBuffer.clear();
				try {
					while (running.get() && (sender = channel.receive(incomingBuffer)) != null) {
						incomingBuffer.flip();
						context.address(sender);
						int packetSize = incomingBuffer.remaining();
						metrics.addIncomingBytes(packetSize);
						UdpFrameworkMessage udpFrameworkMessage =
								udpFramework.readHeader(incomingBuffer, context.csi(), context.ssi());
						if (udpFrameworkMessage != null) {
							metrics.incrementIncomingPacketCount();
							messageHandlers.get(udpFrameworkMessage).handle(context);
						} else {
							metrics.incrementUnknownPacketCount();
							metrics.addUnknownBytes(packetSize);
						}
						incomingBuffer.clear();
					}
				} catch (ClosedChannelException e) {
					if (running.get()) {
						log.error("{} channel closed unexpectedly!", config.name(), e);
						running.set(false);
					}
					break;
				}
				LockSupport.parkNanos(_100us);
			}
		} catch (Exception e) {
			log.error("{} can't start!", "ep." +
					config.name() + "." +
					config.host() + ":" +
					config.port() + "(" +
					effectivePort + ")",
					e
			);
		}
	}

}
