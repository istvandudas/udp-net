package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionMetricsViewTest {

	private static final long NON_ZERO_VALUE = 42L;

	// the test subject
	private ConnectionMetricsView view;

	private ConnectionMetrics metrics;

	@BeforeEach
	void setUp() {
		metrics = new ConnectionMetrics();
		view = metrics.view();
	}

	@Test
	void construction() {
		// GIVEN + WHEN + THEN
		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastSentTime() {
		// GIVEN + WHEN
		metrics.lastSentTime(NON_ZERO_VALUE);

		// THEN
		assertThat(view.lastSentTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastReceivedTime() {
		// GIVEN + WHEN
		metrics.lastReceivedTime(NON_ZERO_VALUE);

		// THEN
		assertThat(view.lastReceivedTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void sentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementSentPacketCount();

		// THEN
		assertThat(view.sentPacketCount()).isEqualTo(1);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void sentBytes() {
		// GIVEN + WHEN
		metrics.addSentBytes(NON_ZERO_VALUE);

		// THEN
		assertThat(view.sentBytes()).isEqualTo(NON_ZERO_VALUE);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void receivedPacketCount() {
		// GIVEN + WHEN
		metrics.incrementReceivedPacketCount();

		// THEN
		assertThat(view.receivedPacketCount()).isEqualTo(1);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void receivedBytes() {
		// GIVEN + WHEN
		metrics.addReceivedBytes(NON_ZERO_VALUE);

		// THEN
		assertThat(view.receivedBytes()).isEqualTo(NON_ZERO_VALUE);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void sentHeartbeatCount() {
		// GIVEN + WHEN
		metrics.incrementSentHeartbeatCount();

		// THEN
		assertThat(view.sentHeartbeatCount()).isEqualTo(1);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void receivedHeartbeatCount() {
		// GIVEN + WHEN
		metrics.incrementReceivedHeartbeatCount();

		// THEN
		assertThat(view.receivedHeartbeatCount()).isEqualTo(1);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.lastReceivedHeartbeatTime()).isZero();
	}

	@Test
	void lastReceivedHeartbeatTime() {
		// GIVEN + WHEN
		metrics.lastReceivedHeartbeatTime(NON_ZERO_VALUE);

		// THEN
		assertThat(view.lastReceivedHeartbeatTime()).isEqualTo(NON_ZERO_VALUE);

		assertThat(view.lastSentTime()).isZero();
		assertThat(view.lastReceivedTime()).isZero();
		assertThat(view.sentPacketCount()).isZero();
		assertThat(view.sentBytes()).isZero();
		assertThat(view.receivedPacketCount()).isZero();
		assertThat(view.receivedBytes()).isZero();
		assertThat(view.sentHeartbeatCount()).isZero();
		assertThat(view.receivedHeartbeatCount()).isZero();
	}
}
