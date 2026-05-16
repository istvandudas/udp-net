package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.Test;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.connection.UdpConnection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.net.endpoint.TestUtil.HOST;
import static org.net.endpoint.TestUtil.waitFor;

class UnreliableUdpEndpointTest extends SenderReceiverTestBase {

	@Test
	void connect() {
		// GIVEN
		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();

		// WHEN
		UdpConnection clientConn = sender.outgoingConnections.getFirst();
		UdpConnection serverConn = receiver.incomingConnections.getFirst();

		// THEN
		assertThat(clientConn.hashCode()).isEqualTo(serverConn.hashCode());
	}

	@Test
	void connection_closedByClient() {
		// GIVEN
		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();
		UdpConnection senderConn = sender.outgoingConnections.getFirst();

		// WHEN
		senderConn.close();

		// THEN
 		assertThat(waitFor(() -> receiver.closedConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 0)).isTrue();
		assertThat(sender.closedConnections.size()).isZero();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 0)).isTrue();
	}

	@Test
	void connection_closedByServer() {
		// GIVEN
		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();
		UdpConnection receiverConn = receiver.incomingConnections.getFirst();

		// WHEN
		receiverConn.close();

		// THEN
		assertThat(waitFor(() -> sender.closedConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 0)).isTrue();
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 0)).isTrue();
		assertThat(receiver.closedConnections.size()).isZero();
	}

	@Test
	void handleCreateConnection_connectionExists() {
		// GIVEN
		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> receiver.incomingConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();

		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 2)).isTrue();
		assertThat(receiver.incomingConnections.size()).isOne();
	}

	@Test
	void handleCreateConnection_noMoreConnection() {
		// GIVEN
		sender.connect(HOST, receiver.effectivePort());

		assertThat(waitFor(() -> receiver.incomingConnections.size(), 1)).isTrue();
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();

		sender.connect(HOST, receiver.effectivePort());
		assertThat(waitFor(() -> sender.outgoingConnections.size(), 2)).isTrue();
		assertThat(receiver.incomingConnections.size()).isOne();
	}

	@Test
	void nonFrameworkPacketReceived() throws IOException {
		// GIVEN
		ByteBuffer buffer = ByteBuffer.allocateDirect(32);
		buffer.put((byte)(UdpFramework.ENDPOINT_DATAGRAM - 1));
		buffer.put(new byte[] {1, 2, 3});

		// WHEN
		sendUdpPacket(new InetSocketAddress(HOST, receiver.effectivePort()), buffer);

		// THEN
		assertThat(waitFor(() -> (int)receiver.metrics().unknownPacketCount(), 1)).isTrue();
		assertThat(waitFor(() -> (int)receiver.metrics().unknownBytes(), 4)).isTrue();
	}

	public static void sendUdpPacket(InetSocketAddress addr, ByteBuffer payload) throws IOException {
		try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
			channel.configureBlocking(false);
			channel.send(payload, addr);
		}
	}

}