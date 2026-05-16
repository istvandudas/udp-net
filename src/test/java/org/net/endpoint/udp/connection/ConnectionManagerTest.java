package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.endpoint.UdpFramework;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
class ConnectionManagerTest {

	private static final byte[] EMPTY_SESSION_ID = new byte[UdpFramework.SESSION_ID_SIZE];
	private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 9999);

	// the test subject
	private ConnectionManager connMgr;

	private UdpFramework udpFramework;
	private Supplier<UdpConnection> connSupplier;
	private Supplier<PendingConnection> pendingConnSupplier;
	private TimeMachine timeMachine;

	private final byte[] csi = new byte[UdpFramework.SESSION_ID_SIZE];
	private final byte[] ssi = new byte[UdpFramework.SESSION_ID_SIZE];

	private final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 5555);
	private final InetSocketAddress portChangedAddr = new InetSocketAddress("127.0.0.1", 6666);
	private final Random random = new Random();

	@BeforeEach
	void setUp() {
		udpFramework = mock(UdpFramework.class);
		connSupplier = mock(Supplier.class);
		pendingConnSupplier = mock(Supplier.class);
		timeMachine = mock(TimeMachine.class);
		connMgr = new ConnectionManager(udpFramework, timeMachine, connSupplier, pendingConnSupplier, 2, 2);
		Arrays.fill(csi, (byte) 42);
		Arrays.fill(ssi, (byte) 43);
		Arrays.fill(EMPTY_SESSION_ID, (byte) 0);
	}

	@Test
	void constructFails_udpFramework_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ConnectionManager(
				null,
				timeMachine,
				connSupplier,
				pendingConnSupplier,
				1,
				1
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("udpFramework is marked non-null but is null");
	}

	@Test
	void constructFails_timeMachine_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ConnectionManager(
				udpFramework,
				null,
				connSupplier,
				pendingConnSupplier,
				1,
				1
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("timeMachine is marked non-null but is null");
	}

	@Test
	void constructFails_connectionSupplier_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ConnectionManager(
				udpFramework,
				timeMachine,
				null,
				pendingConnSupplier,
				1,
				1
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("connectionSupplier is marked non-null but is null");
	}

	@Test
	void constructFails_pendingConnSupplier_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ConnectionManager(
				udpFramework,
				timeMachine,
				connSupplier,
				null,
				1,
				1
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("pendingConnectionSupplier is marked non-null but is null");
	}

	@Test
	void createIncomingConnection() {
		// GIVEN
		long now = random.nextLong();
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(now);

		// WHEN
		UdpConnection actual = connMgr.createIncomingConnection(addr, csi);

		// THEN
		assertThat(actual.address).isEqualTo(addr);
		assertThat(actual.udpFramework).isEqualTo(udpFramework);
		assertThat(actual.connMgr).isEqualTo(connMgr);
		assertThat(actual.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(actual.ssi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(actual.key).isEqualTo(Murmur3.hash128ToLong(actual.csi, actual.ssi));
		assertThat(conn.metrics.view().lastReceivedTime()).isEqualTo(now);
		assertThat(conn.metrics.view().lastSentTime()).isEqualTo(now);
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isZero();
		verify(udpFramework).sendConnectionAccepted(actual.address, actual.csi, actual.ssi);
	}

	@Test
	void createIncomingConnectionRejected_connectionGroupIsFull() {
		// GIVEN
		long now = random.nextLong();
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(now);

		byte[] conn1csi = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(conn1csi, (byte) 99);
		byte[] conn2csi = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(conn1csi, (byte) 98);

		// exhausting incoming ConnectionGroup, has limit 2 in test!
		UdpConnection conn1 = connMgr.createIncomingConnection(
				new InetSocketAddress("1.0.0.1", 10),
				conn1csi
		);
		UdpConnection conn2 = connMgr.createIncomingConnection(
				new InetSocketAddress("1.0.0.2", 11),
				conn2csi
		);

		// WHEN
		UdpConnection actual = connMgr.createIncomingConnection(addr, csi);

		// THEN
		assertThat(conn1).isNotNull();
		assertThat(conn2).isNotNull();
		assertThat(actual).isNull();
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isOne();
		verify(udpFramework).sendConnectionRejected(addr, csi);
	}

	@Test
	void reconnectUpdate() {
		// GIVEN
		long now = random.nextLong();
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(now);
		UdpConnection udpConn = connMgr.createIncomingConnection(addr, csi);
		byte[] originalCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		byte[] originalSsi = new byte[UdpFramework.SESSION_ID_SIZE];
		System.arraycopy(udpConn.csi, 0, originalCsi, 0, UdpFramework.SESSION_ID_SIZE);
		System.arraycopy(udpConn.ssi, 0, originalSsi, 0, UdpFramework.SESSION_ID_SIZE);

		byte[] newCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(newCsi, (byte)142);

		// WHEN
		connMgr.reconnectUpdate(udpConn, newCsi);

		// THEN
		assertThat(udpConn.csi).containsOnly((byte)142);
		assertThat(udpConn.csi).isNotEqualTo(originalCsi);
		assertThat(udpConn.ssi).isNotEqualTo(originalSsi);
	}

	@Test
	void reconnectUpdate_connectionNotFound() {
		// GIVEN
		UdpConnection conn = TestUdpConnection.create();
		byte[] newCsi = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(newCsi, (byte) 142);

		// WHEN
		connMgr.reconnectUpdate(conn, newCsi);

		// THEN
		assertThat(connMgr.metrics().incomingConnectionNotFoundCount()).isOne();
		verify(udpFramework, never()).sendConnectionAccepted(any(), any(), any());
		assertThat(conn.csi).containsOnly((byte) 0);
		assertThat(conn.ssi).containsOnly((byte) 0);
	}

	@Test
	void reconnectUpdate_conn_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.reconnectUpdate(null, csi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void reconnectUpdate_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.reconnectUpdate(mock(UdpConnection.class), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void createIncomingConnection_failed() {
		// GIVEN
		given(connSupplier.get()).willReturn(null);

		// WHEN
		UdpConnection actual = connMgr.createIncomingConnection(addr, csi);

		// THEN
		assertThat(actual).isNull();
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isOne();
	}

	@Test
	void createIncomingConnectionFails_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createIncomingConnection(null, csi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void createIncomingConnectionFails_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createIncomingConnection(addr, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void createPendingOutgoingConnection() {
		// GIVEN
		PendingConnection pendingConn = PendingConnection.create();
		given(pendingConnSupplier.get()).willReturn(pendingConn);

		// WHEN
		PendingConnection actual = connMgr.createPendingOutgoingConnection(addr);

		// THEN
		assertThat(actual).isSameAs(pendingConn);
		assertThat(actual.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(actual.address).isEqualTo(addr);
		assertThat(connMgr.metrics().pendingConnectionFailedCount()).isZero();
	}

	@Test
	void createPendingOutgoingConnection_failed() {
		// GIVEN
		given(pendingConnSupplier.get()).willReturn(null);

		// WHEN
		PendingConnection actual = connMgr.createPendingOutgoingConnection(addr);

		// THEN
		assertThat(actual).isNull();
		assertThat(connMgr.metrics().pendingConnectionFailedCount()).isOne();
	}

	@Test
	void createPendingOutgoingConnectionFails_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createPendingOutgoingConnection(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void createOutgoingConnection() {
		// GIVEN
		long now = random.nextLong();
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(now);
		PendingConnection pendingConn = PendingConnection.create();
		given(pendingConnSupplier.get()).willReturn(pendingConn);
		PendingConnection pConn = connMgr.createPendingOutgoingConnection(addr);

		// WHEN
		UdpConnection actual = connMgr.createOutgoingConnection(addr, pConn.csi, ssi);

		// THEN
		assertThat(actual.address).isEqualTo(addr);
		assertThat(actual.udpFramework).isSameAs(udpFramework);
		assertThat(actual.connMgr).isSameAs(connMgr);
		assertThat(actual.csi).isEqualTo(pConn.csi);
		assertThat(actual.ssi).isEqualTo(ssi);
		assertThat(actual.key).isEqualTo(Murmur3.hash128ToLong(pConn.csi, ssi));
		assertThat(conn.metricsView.lastReceivedTime()).isEqualTo(now);
		assertThat(conn.metricsView.lastSentTime()).isEqualTo(now);
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isZero();
	}

	@Test
	void createOutgoingConnectionFails_cantFindPending() {
		// GIVEN
		PendingConnection pendingConn = PendingConnection.create();
		given(pendingConnSupplier.get()).willReturn(pendingConn);
		PendingConnection pConn = connMgr.createPendingOutgoingConnection(addr);

		// WHEN
		UdpConnection actual = connMgr.createOutgoingConnection(addr, csi, ssi);

		// THEN
		assertThat(csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(pConn.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(pConn.csi).isNotEqualTo(csi);

		assertThat(actual).isNull();
		assertThat(connMgr.metrics().pendingConnectionNotFoundCount()).isOne();
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isOne();
	}

	@Test
	void createOutgoingConnectionFails_cantCreateNewConnection() {
		// GIVEN
		PendingConnection pendingConnection = PendingConnection.create();
		given(pendingConnSupplier.get()).willReturn(pendingConnection);
		PendingConnection pendingConn = connMgr.createPendingOutgoingConnection(addr);
		given(connSupplier.get()).willReturn(null);

		// WHEN
		UdpConnection actual = connMgr.createOutgoingConnection(addr, pendingConn.csi, ssi);

		// THEN
		assertThat(pendingConn.csi).isNotEqualTo(EMPTY_SESSION_ID);

		assertThat(actual).isNull();
		assertThat(connMgr.metrics().pendingConnectionNotFoundCount()).isZero();
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isOne();
	}

	@Test
	void cannotCreateMoreOutgoingConnection() {
		// GIVEN
		long now = random.nextLong();
		given(connSupplier.get()).willReturn(
				TestUdpConnection.create(),
				TestUdpConnection.create(),
				TestUdpConnection.create()
		);
		given(timeMachine.nanoNow()).willReturn(now);
		given(pendingConnSupplier.get()).willReturn(
				PendingConnection.create(),
				PendingConnection.create(),
				PendingConnection.create()
		);

		byte[] ssi1 = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(ssi1, (byte) 0x11);
		byte[] ssi2 = new byte[UdpFramework.SESSION_ID_SIZE];
		Arrays.fill(ssi2, (byte) 0x22);

		PendingConnection pConn1 = connMgr.createPendingOutgoingConnection(new InetSocketAddress("1.0.0.1", 11));
		PendingConnection pConn2 = connMgr.createPendingOutgoingConnection(new InetSocketAddress("1.0.0.2", 22));
		PendingConnection pConn = connMgr.createPendingOutgoingConnection(addr);


		// exhausting outgoing ConnectionGroup, has limit 2 in test!
		UdpConnection conn1 = connMgr.createOutgoingConnection(new InetSocketAddress("1.0.0.1", 11), pConn1.csi, ssi1);
		UdpConnection conn2 = connMgr.createOutgoingConnection(new InetSocketAddress("1.0.0.2", 22), pConn2.csi, ssi2);

		// WHEN
		UdpConnection actual = connMgr.createOutgoingConnection(addr, pConn.csi, ssi);

		// THEN
		assertThat(conn1).isNotNull();
		assertThat(conn2).isNotNull();
		assertThat(actual).isNull();
		assertThat(connMgr.metrics().connectionCreateFailedCount()).isOne();
		verify(udpFramework, never()).sendConnectionRejected(addr, csi);
	}

	@Test
	void createOutgoingConnectionFails_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createOutgoingConnection(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void createOutgoingConnectionFails_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createOutgoingConnection(addr, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void createOutgoingConnectionFails_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.createOutgoingConnection(addr, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void metrics_returnsWithView() {
		// GIVEN + WHEN + THEN
		assertThat(connMgr.metrics()).isInstanceOf(ConnectionManagerMetricsView.class);
	}

	@Test
	void incomingConnectionsFails_connection_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.incomingConnections(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("collectorList is marked non-null but is null");
	}

	@Test
	void outgoingConnectionsFails_connection_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.outgoingConnections(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("collectorList is marked non-null but is null");
	}

	@Test
	void findIncomingConnection_byKey() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);

		// WHEN
		UdpConnection actual = connMgr.findIncoming(Murmur3.hash128ToLong(conn.csi, ssi));

		// THEN
		assertThat(actual).isNotSameAs(conn);
	}

	@Test
	void findIncomingConnection_byAddress() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);

		// WHEN
		UdpConnection actual = connMgr.findIncoming(addr);

		// THEN
		assertThat(actual).isSameAs(conn);
	}

	@Test
	void findIncomingConnection_byAddress_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.findIncoming(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void findIncomingConnection_byCsiSsi() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);

		// WHEN
		UdpConnection actual = connMgr.findIncoming(conn.csi, conn.ssi);

		// THEN
		assertThat(actual).isSameAs(conn);
	}

	@Test
	void findIncomingConnection_byCsiSsi_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.findIncoming(null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void findIncomingConnection_byCsiSsi_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.findIncoming(csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void findOutgoingConnection_byKey() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);

		// WHEN
		UdpConnection actual = connMgr.findOutgoing(Murmur3.hash128ToLong(conn.csi, conn.ssi));

		// THEN
		assertThat(actual).isSameAs(conn);
	}

	@Test
	void closeIncomingConnection() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);

		// WHEN
		boolean actual = connMgr.closeIncomingConnection(conn);

		// THEN
		assertThat(actual).isTrue();
		assertThat(conn.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(conn.csi).isEqualTo(csi);
		assertThat(conn.ssi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(connMgr.metrics().incomingConnectionClosedCount()).isOne();
		assertThat(connMgr.metrics().outgoingConnectionClosedCount()).isZero();
	}

	@Test
	void closeIncomingConnectionFails() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		conn.key = conn.key - 1; // simulate mutated key

		// WHEN
		boolean actual = connMgr.closeIncomingConnection(conn);

		// THEN
		assertThat(actual).isFalse();
		verify(udpFramework, never()).sendConnectionClosed(conn.address, conn.csi, conn.ssi);
		assertThat(connMgr.metrics().incomingConnectionClosedCount()).isZero();
		assertThat(connMgr.metrics().incomingConnectionNotFoundCount()).isOne();
	}

	@Test
	void closeIncomingConnection_connection_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.closeIncomingConnection(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void closeOutgoingConnection() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);

		// WHEN
		boolean actual = connMgr.closeOutgoingConnection(conn);

		// THEN
		assertThat(actual).isTrue();
		assertThat(conn.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(conn.ssi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(connMgr.metrics().incomingConnectionClosedCount()).isZero();
		assertThat(connMgr.metrics().outgoingConnectionClosedCount()).isOne();
	}

	@Test
	void closeOutgoingConnectionFails() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);
		conn.key = conn.key - 1;

		// WHEN
		boolean actual = connMgr.closeOutgoingConnection(conn);

		// THEN
		assertThat(actual).isFalse();
		verify(udpFramework, never()).sendConnectionClosed(conn.address, conn.csi, conn.ssi);
		assertThat(connMgr.metrics().outgoingConnectionClosedCount()).isZero();
		assertThat(connMgr.metrics().outgoingConnectionNotFoundCount()).isOne();
	}

	@Test
	void closeOutgoingConnection_connection_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.closeOutgoingConnection(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void closeConnection_incoming() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);

		// WHEN
		boolean actual = connMgr.closeConnection(conn);

		// THEN
		assertThat(actual).isTrue();
		assertThat(conn.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(conn.ssi).isNotEqualTo(EMPTY_SESSION_ID);
		verify(udpFramework).sendConnectionClosed(conn.address, conn.csi, conn.ssi);
		assertThat(connMgr.metrics().incomingConnectionClosedCount()).isOne();
	}

	@Test
	void closeConnection_outgoing() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);

		// WHEN
		boolean actual = connMgr.closeConnection(conn);

		// THEN
		assertThat(actual).isTrue();
		assertThat(conn.csi).isNotEqualTo(EMPTY_SESSION_ID);
		assertThat(conn.ssi).isNotEqualTo(EMPTY_SESSION_ID);
		verify(udpFramework).sendConnectionClosed(conn.address, conn.csi, conn.ssi);
		assertThat(connMgr.metrics().outgoingConnectionClosedCount()).isOne();
	}

	@Test
	void closeConnectionFails() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);
		conn.key = conn.key - 1;

		// WHEN
		boolean actual = connMgr.closeConnection(conn);

		// THEN
		assertThat(actual).isFalse();
		verify(udpFramework, never()).sendConnectionClosed(conn.address, conn.csi, conn.ssi);
		assertThat(connMgr.metrics().incomingConnectionClosedCount()).isZero();
		assertThat(connMgr.metrics().outgoingConnectionClosedCount()).isZero();
	}

	@Test
	void closeConnection_conn_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.closeConnection(null))
				.isInstanceOf(NullPointerException.class)
						.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void closePendingOutgoingConnection() {
		// GIVEN
		PendingConnection pendingConnection = givenPendingConnection(addr);

		// WHEN
		boolean actual = connMgr.closePendingOutgoingConnection(pendingConnection.csi);

		// THEN
		assertThat(actual).isTrue();
		assertThat(connMgr.metrics().pendingConnectionNotFoundCount()).isZero();
	}

	@Test
	void closePendingOutgoingConnectionFails() {
		// GIVEN + WHEN
		boolean actual = connMgr.closePendingOutgoingConnection(csi);

		// THEN
		assertThat(actual).isFalse();
		assertThat(connMgr.metrics().pendingConnectionNotFoundCount()).isOne();
	}

	@Test
	void closePendingOutgoingConnection_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.closePendingOutgoingConnection(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void heartbeatReceived() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		reset(timeMachine);
		given(timeMachine.nanoNow()).willReturn(2L);

		// WHEN
		connMgr.heartbeatReceived(conn.address, conn.csi, conn.ssi);

		// THEN
		assertThat(conn.metrics().lastReceivedHeartbeatTime()).isEqualTo(2L);
	}

	@Test
	void heartbeatReceived_connIsNotFound() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		reset(timeMachine);

		// WHEN
		connMgr.heartbeatReceived(conn.address, conn.csi, ssi);

		// THEN
		assertThat(connMgr.metrics().incomingConnectionNotFoundCount()).isOne();
	}

	@Test
	void heartbeatReceivedFails_sender_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.heartbeatReceived(null, csi, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("sender is marked non-null but is null");
	}

	@Test
	void heartbeatReceivedFails_csi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.heartbeatReceived(ADDRESS, null, ssi))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("csi is marked non-null but is null");
	}

	@Test
	void heartbeatReceivedFails_ssi_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.heartbeatReceived(ADDRESS, csi, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ssi is marked non-null but is null");
	}

	@Test
	void portCanChange_heartbeat() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		reset(timeMachine);
		given(timeMachine.nanoNow()).willReturn(2L);

		// WHEN
		connMgr.heartbeatReceived(portChangedAddr, conn.csi, conn.ssi);

		// THEN
		assertThat(conn.metrics().lastReceivedHeartbeatTime()).isEqualTo(2L);
		assertThat(conn.address).isEqualTo(portChangedAddr);
	}

	@Test
	void dataReceived() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		reset(timeMachine);

		// WHEN
		connMgr.dataReceived(conn.csi, conn.ssi, 42);

		// THEN
		assertThat(conn.metrics().receivedPacketCount()).isEqualTo(1L);
		assertThat(conn.metrics().receivedBytes()).isEqualTo(42L);
	}

	@Test
	void dataReceived_connectionNotFound() {
		// GIVEN
		UdpConnection conn = givenIncomingConnection(addr, csi);
		reset(timeMachine);

		// WHEN
		connMgr.dataReceived(conn.csi, ssi, 42);

		// THEN
		assertThat(conn.metrics().receivedPacketCount()).isEqualTo(0);
		assertThat(conn.metrics().receivedBytes()).isEqualTo(0);
	}

	@Test
	void sendHeartbeat() {
		// GIVEN
		UdpConnection conn = givenOutgoingConnection(addr);

		// WHEN
		connMgr.sendHeartbeat(conn);

		// THEN
		assertThat(conn.metrics().sentHeartbeatCount()).isOne();
		verify(udpFramework).sendHeartbeat(conn.address, conn.csi, conn.ssi);
	}

	@Test
	void sendHeartbeatFails() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> connMgr.sendHeartbeat(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	private UdpConnection givenIncomingConnection(InetSocketAddress address, byte[] csi) {
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(1L);
		return connMgr.createIncomingConnection(address, csi);
	}

	private UdpConnection givenOutgoingConnection(InetSocketAddress address) {
		PendingConnection pendingConnection = givenPendingConnection(address);
		UdpConnection conn = TestUdpConnection.create();
		given(connSupplier.get()).willReturn(conn);
		given(timeMachine.nanoNow()).willReturn(System.nanoTime());
		return connMgr.createOutgoingConnection(address, pendingConnection.csi, ssi);
	}

	private PendingConnection givenPendingConnection(InetSocketAddress address) {
		PendingConnection pendingConnection = PendingConnection.create();
		given(pendingConnSupplier.get()).willReturn(pendingConnection);
		return connMgr.createPendingOutgoingConnection(address);
	}

}
