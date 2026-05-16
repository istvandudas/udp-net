package org.net.endpoint.udp.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.udp.endpoint.UdpEndpoint;
import org.net.endpoint.udp.endpoint.UdpFramework;
import org.net.endpoint.udp.endpoint.UdpFrameworkMessage;
import org.net.endpoint.udp.sender.PacketSender;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DataFlowIssue"})
class UdpFrameworkTest {

	// the test subject
	private UdpFramework framework;

	private PacketSender sender;
	private final InetSocketAddress ADDRESS = new InetSocketAddress("127.0.0.1", 9999);

	private byte[] csi;
	private byte[] ssi;
	private ByteBuffer cmdBuffer;
	private BufferPool bufferPool;

	@BeforeEach
	void setUp() {
		sender = mock(PacketSender.class);
		cmdBuffer = ByteBuffer.allocate(UdpEndpoint.FRAMEWORK_CMD_SIZE);
		bufferPool = mock(BufferPool.class);
		given(bufferPool.createForCmd()).willReturn(cmdBuffer);
		framework = new UdpFramework(bufferPool, sender);

		csi = new byte[UdpFramework.SESSION_ID_SIZE];
		ssi = new byte[UdpFramework.SESSION_ID_SIZE];

		Arrays.fill(csi, (byte) 0x11);
		Arrays.fill(ssi, (byte) 0x22);
	}

	@Test
	void sendCreateConnection() {
		// GIVEN
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);

		// WHEN
		framework.sendCreateConnection(ADDRESS, csi);

		// THEN
		verify(sender).send(captor.capture(), eq(ADDRESS), eq(true));

		ByteBuffer buf = captor.getValue();
		buf.position(0); // reset read pointer
		buf.limit(buf.capacity());

		assertThat(buf.get()).isEqualTo(UdpFramework.ENDPOINT_DATAGRAM);

		byte type = buf.get();
		assertThat(type & 0b111).isEqualTo(UdpFrameworkMessage.CreateConnection.type());

		byte[] readCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		buf.get(readCsi, 0, UdpFramework.SESSION_ID_SIZE);
		assertThat(readCsi).containsExactly(csi);

		// SSI must NOT be written → next bytes remain zero
		byte[] readSsi = new byte[UdpFramework.SESSION_ID_SIZE];
		buf.get(readSsi, 0, UdpFramework.SESSION_ID_SIZE);
		assertThat(readSsi).containsOnly((byte) 0);
	}

	@Test
	void sendCreateConnection_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendCreateConnection(null, csi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendCreateConnection_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendCreateConnection(ADDRESS, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendConnectionAccepted() {
		// GIVEN
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);


		// WHEN
		framework.sendConnectionAccepted(ADDRESS, csi, ssi);

		// THEN
		verify(sender).send(captor.capture(), eq(ADDRESS), eq(true));

		ByteBuffer buf = captor.getValue();
		buf.position(0);

		assertThat(buf.get()).isEqualTo(UdpFramework.ENDPOINT_DATAGRAM);

		byte type = buf.get();
		assertThat(type & 0b111).isEqualTo(UdpFrameworkMessage.ConnectionAccepted.type());

		byte[] readCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] readSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		buf.get(readCsi, 0, UdpFramework.SESSION_ID_SIZE);
		buf.get(readSsi, 0, UdpFramework.SESSION_ID_SIZE);

		assertThat(readCsi).containsExactly(csi);
		assertThat(readSsi).containsExactly(ssi);
	}

	@Test
	void sendConnectionAccepted_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionAccepted(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendConnectionAccepted_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionAccepted(ADDRESS, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendConnectionAccepted_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionAccepted(ADDRESS, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void sendConnectionRejected() {
		// GIVEN
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);


		// WHEN
		framework.sendConnectionRejected(ADDRESS, csi);

		// THEN
		verify(sender).send(captor.capture(), eq(ADDRESS), eq(true));

		ByteBuffer buf = captor.getValue();
		buf.position(0);

		assertThat(buf.get()).isEqualTo(UdpFramework.ENDPOINT_DATAGRAM);

		byte type = buf.get();
		assertThat(type & 0b111).isEqualTo(UdpFrameworkMessage.ConnectionRejected.type());

		byte[] readCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		buf.get(readCsi, 0, UdpFramework.SESSION_ID_SIZE);
		assertThat(readCsi).containsExactly(csi);

		assertThat(buf.position()).isEqualTo(18);
	}

	@Test
	void sendConnectionRejected_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionRejected(null, csi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendConnectionRejected_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionRejected(ADDRESS, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendConnectionClosed() {
		// GIVEN
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);


		// WHEN
		framework.sendConnectionClosed(ADDRESS, csi, ssi);

		// THEN
		verify(sender).send(captor.capture(), eq(ADDRESS), eq(true));

		ByteBuffer buf = captor.getValue();
		buf.position(0);

		assertThat(buf.get()).isEqualTo(UdpFramework.ENDPOINT_DATAGRAM);

		byte type = buf.get();
		assertThat(type & 0b111).isEqualTo(UdpFrameworkMessage.ConnectionClosed.type());

		byte[] readCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] readSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		buf.get(readCsi, 0, UdpFramework.SESSION_ID_SIZE);
		buf.get(readSsi, 0, UdpFramework.SESSION_ID_SIZE);

		assertThat(readCsi).containsExactly(csi);
		assertThat(readSsi).containsExactly(ssi);
	}

	@Test
	void sendConnectionClosed_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionClosed(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendConnectionClosed_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionClosed(ADDRESS, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendConnectionClosed_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendConnectionClosed(ADDRESS, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void sendHeartbeat() {
		// GIVEN
		ArgumentCaptor<ByteBuffer> captor = ArgumentCaptor.forClass(ByteBuffer.class);


		// WHEN
		framework.sendHeartbeat(ADDRESS, csi, ssi);

		// THEN
		verify(sender).send(captor.capture(), eq(ADDRESS), eq(true));

		ByteBuffer buf = captor.getValue();
		buf.position(0);

		assertThat(buf.get()).isEqualTo(UdpFramework.ENDPOINT_DATAGRAM);

		byte type = buf.get();
		assertThat(type & 0b111).isEqualTo(UdpFrameworkMessage.Heartbeat.type());
	}

	@Test
	void sendHeartbeat_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendHeartbeat(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendHeartbeat_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendHeartbeat(ADDRESS, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendHeartbeat_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendHeartbeat(ADDRESS, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void sendData() {
		// GIVEN
		ByteBuffer data = ByteBuffer.allocateDirect(2048).position(142).flip();
		ByteBuffer buffer = ByteBuffer.allocateDirect(1200);
		given(bufferPool.createForData()).willReturn(buffer);
		given(sender.send(any(), any(), eq(true))).willReturn(true);

		// WHEN
		int actual = framework.sendData(ADDRESS, csi, ssi, data);

		// THEN
		assertThat(actual).isEqualTo(142 + 34);
		verify(sender).send(eq(buffer), eq(ADDRESS), eq(true));
	}

	@Test
	void sendData_cannotCreateMoreSendRequest() {
		// GIVEN
		ByteBuffer data = ByteBuffer.allocateDirect(2048).position(142).flip();
		ByteBuffer buffer = ByteBuffer.allocateDirect(1200);
		given(bufferPool.createForData()).willReturn(buffer);
		given(sender.send(any(), any(), eq(true))).willReturn(false);

		// WHEN
		int actual = framework.sendData(ADDRESS, csi, ssi, data);

		// THEN
		assertThat(actual).isZero();
		verify(sender).send(eq(buffer), eq(ADDRESS), eq(true));
	}

	@Test
	void sendData_tooBigData() {
		// GIVEN
		ByteBuffer data = ByteBuffer.allocateDirect(128);
		data.position(100);
		data.flip();
		ByteBuffer buffer = ByteBuffer.allocate(64);
		given(bufferPool.createForData()).willReturn(buffer);

		// WHEN
		framework.sendData(ADDRESS, csi, ssi, data);

		// THEN
		verify(sender, never()).send(any(), any(), anyBoolean());
	}

	@Test
	void sendData_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendData(null, csi, ssi, cmdBuffer))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void sendData_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendData(ADDRESS, null, ssi, cmdBuffer))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void sendData_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendData(ADDRESS, csi, null, cmdBuffer))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void sendData_buffer_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.sendData(ADDRESS, csi, ssi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("buffer is marked non-null but is null");
	}

	@Test
	void readHeader_createConnection() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.CreateConnection.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.CreateConnection);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsOnly((byte) 0);
	}

	@Test
	void readHeader_connectionAccepted() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.ConnectionAccepted.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);
		cmdBuffer.put(ssi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.ConnectionAccepted);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsExactly(ssi);
	}

	@Test
	void readHeader_connectionRejected() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.ConnectionRejected.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.ConnectionRejected);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsOnly((byte) 0);
	}

	@Test
	void readHeader_connectionClosed() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.ConnectionClosed.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);
		cmdBuffer.put(ssi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.ConnectionClosed);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsExactly(ssi);
	}

	@Test
	void readHeader_data() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.Data.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);
		cmdBuffer.put(ssi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.Data);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsExactly(ssi);
	}

	@Test
	void readHeader_heartbeat() {
		// GIVEN
		cmdBuffer.put(UdpFramework.ENDPOINT_DATAGRAM);
		cmdBuffer.put(UdpFrameworkMessage.Heartbeat.type());
		cmdBuffer.put(csi, 0, UdpFramework.SESSION_ID_SIZE);
		cmdBuffer.put(ssi, 0, UdpFramework.SESSION_ID_SIZE);

		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isEqualTo(UdpFrameworkMessage.Heartbeat);
		assertThat(outCsi).containsExactly(csi);
		assertThat(outSsi).containsExactly(ssi);
	}

	@Test
	void readHeader_invalidDatagram_returnsNull() {
		// GIVEN
		cmdBuffer.put((byte) 0x00); // invalid
		cmdBuffer.position(0);

		byte[] outCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] outSsi = new byte[UdpFramework.SESSION_ID_SIZE];

		// WHEN
		UdpFrameworkMessage msg = framework.readHeader(cmdBuffer, outCsi, outSsi);

		// THEN
		assertThat(msg).isNull();
	}

	@Test
	void readHeader_buffer_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.readHeader(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("buffer is marked non-null but is null");
	}

	@Test
	void readHeader_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.readHeader(cmdBuffer, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void readHeader_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> framework.readHeader(cmdBuffer, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void createConnection_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForCmd()).willReturn(null);

		// WHEN
		framework.sendCreateConnection(ADDRESS, csi);

		// THEN
		verify(sender, never()).send(any(), any(), eq(true));
	}

	@Test
	void connectionAccepted_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForCmd()).willReturn(null);

		// WHEN
		framework.sendConnectionAccepted(ADDRESS, csi, ssi);

		// THEN
		verify(sender, never()).send(any(), any(), eq(true));
	}

	@Test
	void connectionRejected_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForCmd()).willReturn(null);

		// WHEN
		framework.sendConnectionRejected(ADDRESS, csi);

		// THEN
		verify(sender, never()).send(any(), any(), eq(true));
	}

	@Test
	void connectionClosed_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForCmd()).willReturn(null);

		// WHEN
		framework.sendConnectionClosed(ADDRESS, csi, ssi);

		// THEN
		verify(sender, never()).send(any(), any(), eq(true));
	}

	@Test
	void heartbeat_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForCmd()).willReturn(null);

		// WHEN
		framework.sendHeartbeat(ADDRESS, csi, ssi);

		// THEN
		verify(sender, never()).send(any(), any(), eq(true));
	}

	@Test
	void sendData_cannotCreateMoreBuffer() {
		// GIVEN
		given(bufferPool.createForData()).willReturn(null);
		ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
		buffer.position(142).flip();

		// WHEN
		int actual = framework.sendData(ADDRESS, csi, ssi, buffer);

		// THEN
		assertThat(actual).isZero();
		verify(sender, never()).send(any(), any(), eq(true));
	}
}