package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UnreliableUdpConnectionTest {

	@Test
	void create_useBorrow() {
		// GIVEN + WHEN
		UdpConnection conn = UnreliableUdpConnection.create();

		// THEN
		assertThat(conn).isInstanceOf(UnreliableUdpConnection.class);
		assertThat(UnreliableUdpConnection.poolStat().get()).isGreaterThan(0);
		assertThat(UnreliableUdpConnection.poolStat().create()).isGreaterThan(0);
		assertThat(UnreliableUdpConnection.poolStat().size()).isZero();
	}

	@Test
	void release_resetTheStateAndReturnToPool() {
		// GIVEN
		UnreliableUdpConnection conn = (UnreliableUdpConnection) UnreliableUdpConnection.create();
		Arrays.fill(conn.csi, (byte) 0x7F);
		Arrays.fill(conn.ssi, (byte) 0x55);

		// WHEN
		conn.release();

		// THEN
		assertThat(conn.csi).containsOnly((byte) 0);
		assertThat(conn.ssi).containsOnly((byte) 0);

		// And verify reuse
		UnreliableUdpConnection conn2 = (UnreliableUdpConnection) UnreliableUdpConnection.create();
		assertThat(conn2).isSameAs(conn);
	}

	@Test
	void equals_useCsiAndSsiArrays() {
		// GIVEN
		UnreliableUdpConnection c1 = (UnreliableUdpConnection) UnreliableUdpConnection.create();
		UnreliableUdpConnection c2 = (UnreliableUdpConnection) UnreliableUdpConnection.create();

		Arrays.fill(c1.csi, (byte) 0x22);
		Arrays.fill(c1.ssi, (byte) 0x33);

		Arrays.fill(c2.csi, (byte) 0x22);
		Arrays.fill(c2.ssi, (byte) 0x33);

		// WHEN
		assertThat(c1).isEqualTo(c2);
		assertThat(c1.hashCode()).isEqualTo(c2.hashCode());

		// THEN
		c2.csi[0] = 0x44;
		assertThat(c1).isNotEqualTo(c2);
	}

	@Test
	void equals_withDifferentType() {
		// GIVEN
		UnreliableUdpConnection c1 = (UnreliableUdpConnection) UnreliableUdpConnection.create();

		// WHEN
		boolean actual = c1.equals(new Object());

		// THEN
		assertThat(actual).isFalse();
	}

	@Test
	void reuseMultipleTimes() {
		// GIVEN + WHEN
		UnreliableUdpConnection c1 = (UnreliableUdpConnection) UnreliableUdpConnection.create();
		c1.release();

		UnreliableUdpConnection c2 = (UnreliableUdpConnection) UnreliableUdpConnection.create();
		c2.release();

		UnreliableUdpConnection c3 = (UnreliableUdpConnection) UnreliableUdpConnection.create();

		// THEN
		assertThat(c3).isSameAs(c1);
	}

	@Test
	void stat() {
		// GIVEN + WHEN
		String actual = UnreliableUdpConnection.stat();

		// THEN
		assertThat(actual).startsWith("UnreliableUdpConnection[");
		assertThat(actual).endsWith("]");
	}
}
