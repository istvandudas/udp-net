package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class PendingConnectionTest {

	@Test
	void create_useBorrow() {
		// GIVEN + WHEN
		PendingConnection pc = PendingConnection.create();

		// THEN
		assertThat(PendingConnection.poolStat().get()).isGreaterThan(0);
		assertThat(PendingConnection.poolStat().create()).isGreaterThan(0);
		assertThat(PendingConnection.poolStat().size()).isZero();
	}

	@Test
	void release_resetTheStateAndReturnToPool() {
		// GIVEN
		PendingConnection pc = givenPendingConnection("127.0.0.1", 1234);
		Arrays.fill(pc.csi(), (byte) 0x7F);

		// WHEN
		pc.release();

		// THEN
		assertThat(pc.address()).isNull();
		assertThat(pc.csi()).containsOnly((byte) 0);

		PendingConnection pc2 = givenPendingConnection("1.2.3.4", 9999);
		assertThat(pc2).isSameAs(pc);
	}

	@Test
	void equals_useOnlyCsi() {
		// GIVEN
		PendingConnection pc1 = givenPendingConnection("1.1.1.1", 1000);
		PendingConnection pc2 = givenPendingConnection("2.2.2.2", 2000);
		Arrays.fill(pc1.csi(), (byte) 0x22);
		Arrays.fill(pc2.csi(), (byte) 0x22);

		// WHEN
		assertThat(pc1).isEqualTo(pc2);
		assertThat(pc1.hashCode()).isEqualTo(pc2.hashCode());

		// THEN
		pc2.csi()[0] = 0x33;
		assertThat(pc1).isNotEqualTo(pc2);
	}

	@Test
	void equals_withDifferentType() {
		// GIVEN
		PendingConnection pc1 = givenPendingConnection("1.1.1.1", 1000);

		// WHEN
		boolean actual = pc1.equals(new Object());

		// THEN
		assertThat(actual).isFalse();
	}

	@Test
	void reuseMultipleTimes() {
		// GIVEN + WHEN
		PendingConnection pc = givenPendingConnection("1.1.1.1", 1000);
		pc.release();
		PendingConnection pc2 = givenPendingConnection("2.2.2.2", 2000);
		pc2.release();
		PendingConnection pc3 = givenPendingConnection("3.3.3.3", 3000);

		// THEN
		assertThat(pc3).isSameAs(pc);
	}

	@Test
	void stat() {
		// GIVEN + WHEN
		String actual = PendingConnection.stat();

		// THEN
		assertThat(actual).startsWith("PendingConnection[");
		assertThat(actual).endsWith("]");
	}

	private PendingConnection givenPendingConnection(String host, int port) {
		PendingConnection pendingConnection = PendingConnection.create();
		pendingConnection.address = new InetSocketAddress(host, port);
		return pendingConnection;
	}
}
