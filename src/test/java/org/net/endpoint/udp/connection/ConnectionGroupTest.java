package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
class ConnectionGroupTest {

	private static final InetSocketAddress ADDRESS_1 = new InetSocketAddress("127.0.0.1", 1111);
	private static final InetSocketAddress ADDRESS_2 = new InetSocketAddress("127.0.0.1", 2222);

	// the test subject
	private ConnectionGroup group;

	@BeforeEach
	void setUp() {
		group = new ConnectionGroup();
	}

	@Test
	void add_lookupByKey() {
		// GIVEN
		UdpConnection conn = givenConnection(123, ADDRESS_1);

		// WHEN
		group.add(conn);

		// THEN
		assertThat(group.lookup(123)).isSameAs(conn);
		assertThat(group.size()).isEqualTo(1);
	}

	@Test
	void add_lookupByAddress() {
		// GIVEN
		UdpConnection conn = givenConnection(555, ADDRESS_1);

		// WHEN
		group.add(conn);

		// THEN
		assertThat(group.lookup(ADDRESS_1)).isSameAs(conn);
	}

	@Test
	void remove_deletesFromBothMaps_butNotReleaseConnection() {
		// GIVEN
		UdpConnection conn = givenConnection(999, ADDRESS_1);
		group.add(conn);

		// WHEN
		boolean actual = group.remove(conn);

		// THEN
		assertThat(actual).isTrue();
		assertThat(group.lookup(999L)).isNull();
		assertThat(group.lookup(ADDRESS_1)).isNull();
		verify(conn, never()).release();
		assertThat(group.size()).isEqualTo(0);
	}

	@Test
	void remove_returnsFalseIfNotPresent() {
		// GIVEN
		UdpConnection conn = givenConnection(1, ADDRESS_1);

		// WHEN + THEN
		assertThat(group.remove(conn)).isFalse();
	}

	@Test
	void updatePort_movesConnectionInAddressMap() {
		// GIVEN
		UdpConnection conn = givenConnection(42, ADDRESS_1);
		group.add(conn);

		// WHEN
		group.updatePort(conn, ADDRESS_2);

		// THEN
		assertThat(group.lookup(ADDRESS_1)).isNull();
		assertThat(group.lookup(ADDRESS_2)).isSameAs(conn);
		assertThat(conn.address).isEqualTo(ADDRESS_2);
	}

	@Test
	void connections_collectsAllConnections() {
		// GIVEN
		UdpConnection c1 = givenConnection(1, ADDRESS_1);
		UdpConnection c2 = givenConnection(2, ADDRESS_2);
		group.add(c1);
		group.add(c2);

		// WHEN
		List<UdpConnection> actual = new ArrayList<>();
		group.connections(actual);

		assertThat(actual).containsExactlyInAnyOrder(c1, c2);
	}

	@Test
	void lookup_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> group.lookup(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}

	@Test
	void remove_conn_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> group.remove(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void updatePort_conn_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> group.updatePort(null, ADDRESS_1))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("conn is marked non-null but is null");
	}

	@Test
	void updatePort_address_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> group.updatePort(mock(UdpConnection.class), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("address is marked non-null but is null");
	}


	private UdpConnection givenConnection(long key, InetSocketAddress addr) {
		UdpConnection conn = mock(UdpConnection.class);
		conn.key = key;
		given(conn.address()).willReturn(addr);
		conn.address = addr;
		return conn;
	}

}