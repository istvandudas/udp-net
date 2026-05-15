package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.udp.endpoint.UdpFramework;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings("DataFlowIssue")
class UdpConnectionTest {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 5555;
	private static final InetSocketAddress ADDRESS = new InetSocketAddress(HOST_NAME, PORT);

	// the test subject
	private UdpConnection connection;

	private UdpFramework udpFramework;
	private ConnectionManager connMgr;

	@BeforeEach
	void setUp() {
		udpFramework = mock(UdpFramework.class);
		connMgr = mock(ConnectionManager.class);
		connection = TestUdpConnection.create();
	}

	@Test
	void address() {
		// GIVEN
		connection.address = ADDRESS;

		// WHEN
		InetSocketAddress actual = connection.address();

		// THEN
		assertThat(actual.getHostName()).isEqualTo(HOST_NAME);
		assertThat(actual.getPort()).isEqualTo(PORT);
	}

	@Test
	void reset() {
		// GIVEN
		givenConnection();

		// WHEN
		connection.reset();

		// THEN
		assertThat(connection.address).isNull();
		assertThat(connection.connMgr).isNull();
		assertThat(connection.udpFramework).isNull();
		assertThat(connection.key).isZero();
		assertThat(connection.csi).containsOnly((byte) 0);
		assertThat(connection.ssi).containsOnly((byte) 0);
	}

	@Test
	void metrics() {
		// GIVEN
		givenConnection();

		// WHEN
		Object actual = connection.metrics();

		// THEN
		assertThat(actual).isInstanceOf(ConnectionMetricsView.class);
	}

	/**
	 * FIXME Ignored while sendData is not implemented
	@Test
	void send() {
		// GIVEN
		givenConnection();

		// WHEN
		connection.send(buffer);

		// THEN
		verify(udpFramework).sendData(connection.address, buffer);
	}
	 */

	@Test
	void natPortUpdate() {
		// GIVEN
		givenConnection();

		// WHEN
		InetSocketAddress portChangedAddress = new InetSocketAddress(HOST_NAME, 6666);
		connection.natPortUpdate(portChangedAddress);

		// THEN
		assertThat(connection.address.getHostName()).isEqualTo(HOST_NAME);
		assertThat(connection.address.getPort()).isEqualTo(6666);
		assertThat(connection.address).isSameAs(portChangedAddress);
	}

	@Test
	void natPortUpdate_happensNothing() {
		// GIVEN
		givenConnection();

		// WHEN
		InetSocketAddress portChangedAddress = new InetSocketAddress(HOST_NAME, PORT);
		connection.natPortUpdate(portChangedAddress);

		// THEN
		assertThat(connection.address.getHostName()).isEqualTo(HOST_NAME);
		assertThat(connection.address.getPort()).isEqualTo(PORT);
		assertThat(connection.address).isNotSameAs(portChangedAddress);
	}


	@Test
	void natPortUpdateFails_onNotInitializedAddress() {
		// GIVEN
		givenConnection();
		connection.address = null;

		// WHEN + THEN
		assertThatThrownBy(() -> connection.natPortUpdate(new InetSocketAddress(HOST_NAME, 6666)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("address need to be initialized first!");
	}

	@Test
	void natPortUpdateFails_ifParamAddressIsNull() {
		// GIVEN
		givenConnection();

		// WHEN + THEN
		assertThatThrownBy(() -> connection.natPortUpdate(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void natPortUpdateFails_hostnameMismatch() {
		// GIVEN
		givenConnection();

		// WHEN + THEN
		assertThatThrownBy(() -> connection.natPortUpdate(new InetSocketAddress("not-localhost", 6666)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("hostname doesn't match!");
	}

	@Test
	void hashCode_covered() {
		// GIVEN
		givenConnection();

		// WHEN
		int hash = connection.hashCode();

		// THEN
		assertThat(hash).isEqualTo(Long.hashCode(connection.key));
	}

	@Test
	void equals_notSameType() {
		// GIVEN
		UdpConnection conn1 = givenConnection(42, 43);
		UdpConnection conn2 = givenConnection(43, 44);

		// WHEN + THEN
		assertThat(conn1.equals(conn2)).isFalse();
	}

	@Test
	void equals_csiMismatch() {
		TestUdpConnection con1 = TestUdpConnection.create();
		TestUdpConnection con2 = TestUdpConnection.create();

		Arrays.fill(con1.csi, (byte)1);
		Arrays.fill(con2.csi, (byte)2);

		assertThat(con1.equals(con2)).isFalse();
	}

	@Test
	void equals_withDifferentType() {
		TestUdpConnection con1 = TestUdpConnection.create();

		Arrays.fill(con1.csi, (byte)1);

		assertThat(con1.equals(new Object())).isFalse();
	}

	@Test
	void equals_ssiMismatch() {
		TestUdpConnection con1 = TestUdpConnection.create();
		TestUdpConnection con2 = TestUdpConnection.create();

		Arrays.fill(con1.ssi, (byte)1);
		Arrays.fill(con2.ssi, (byte)2);

		assertThat(con1.equals(con2)).isFalse();
	}

	@Test
	void equals() {
		// GIVEN
		TestUdpConnection con1 = TestUdpConnection.create();
		Arrays.fill(con1.csi, (byte)42);
		Arrays.fill(con1.ssi, (byte)43);

		TestUdpConnection con2 = TestUdpConnection.create();
		Arrays.fill(con2.csi, (byte)42);
		Arrays.fill(con2.ssi, (byte)43);

		// WHEN
		boolean actual = con1.equals(con2);

		// THEN
		assertThat(actual).isTrue();
		assertThat(con1).isNotSameAs(con2);
	}

	@Test
	void close() {
		// GIVEN
		givenConnection();

		// WHEN
		connection.close();

		// THEN
		verify(connMgr).closeConnection(connection);
	}

	@Test
	void send() {
		// GIVEN
		givenConnection();

		ByteBuffer buffer = ByteBuffer.allocateDirect(128);
		buffer.position(100).flip();

		// WHEN
		connection.send(buffer);

		// THEN
		verify(udpFramework).sendData(connection.address, connection.csi, connection.ssi, buffer);
	}

	@Test
	void send_buffer_isNull() {
		// GIVEN
		givenConnection();

		// WHEN + THEN
		assertThatThrownBy(() -> connection.send(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("buffer is marked non-null but is null");
	}

	private void givenConnection() {
		connection.address = ADDRESS;
		Arrays.fill(connection.csi, (byte) 42);
		Arrays.fill(connection.ssi, (byte) 43);
		connection.key = Murmur3.hash128ToLong(connection.csi, connection.ssi);
		connection.connMgr = connMgr;
		connection.udpFramework = udpFramework;
	}

	private UdpConnection givenConnection(int csiFiller, int ssiFiller) {
		UdpConnection connection = TestUdpConnection.create();
		connection.address = ADDRESS;
		Arrays.fill(connection.csi, (byte) csiFiller);
		Arrays.fill(connection.ssi, (byte) ssiFiller);
		connection.key = Murmur3.hash128ToLong(connection.csi, connection.ssi);
		connection.connMgr = connMgr;
		connection.udpFramework = udpFramework;
		return connection;
	}

}