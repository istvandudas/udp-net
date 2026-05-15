package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionMetricsTest {

	private static final long NON_ZERO_VALUE = 42L;

	// the test subject
	private ConnectionMetrics metrics;

	@BeforeEach
	void setUp() {
		metrics = new ConnectionMetrics();
	}

	@Test
	void construct() {
		// GIVEN + WHEN + THEN
		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastSentTime() {
		// GIVEN + WHEN
		metrics.lastSentTime(NON_ZERO_VALUE);

		// THEN
		assertThat(metrics.lastSentTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastReceivedTime() {
		// GIVEN + WHEN
		metrics.lastReceivedTime(NON_ZERO_VALUE);

		// THEN
		assertThat(metrics.lastReceivedTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void incrementSentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementSentPacketCount();

		// WHEN
		assertThat(metrics.sentPacketCount()).isEqualTo(1);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void addSentBytes() {
		// GIVEN + WHEN
		metrics.addSentBytes(NON_ZERO_VALUE);

		// THEN
		assertThat(metrics.sentBytes()).isEqualTo(NON_ZERO_VALUE);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void incrementReceivedPacketCount() {
		// GIVEN + WHEN
		metrics.incrementReceivedPacketCount();

		// THEN
		assertThat(metrics.receivedPacketCount()).isEqualTo(1);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void addReceivedBytes() {
		// GIVEN + WHEN
		metrics.addReceivedBytes(NON_ZERO_VALUE);

		// THEN
		assertThat(metrics.receivedBytes()).isEqualTo(NON_ZERO_VALUE);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void incrementSentHeartbeatCount() {
		// GIVEN + WHEN
		metrics.incrementSentHeartbeatCount();

		// THEN
		assertThat(metrics.sentHeartbeatCount()).isEqualTo(1);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void incrementReceivedHeartbeatCount() {
		// GIVEN + WHEN
		metrics.incrementReceivedHeartbeatCount();

		// THEN
		assertThat(metrics.receivedHeartbeatCount()).isEqualTo(1);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastReceivedHeartbeatTime() {
		// GIVEN + WHEN
		metrics.lastReceivedHeartbeatTime(NON_ZERO_VALUE);

		// THEN
		assertThat(metrics.lastReceivedHeartbeatTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(metrics.lastSentTime()).isZero();
		assertThat(metrics.lastReceivedTime()).isZero();
		assertThat(metrics.sentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.receivedPacketCount()).isZero();
		assertThat(metrics.receivedBytes()).isZero();
		assertThat(metrics.sentHeartbeatCount()).isZero();
		assertThat(metrics.receivedHeartbeatCount()).isZero();
	}
}
