package org.net.endpoint.udp.connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionManagerMetricsTest {

	private ConnectionManagerMetrics metrics;
	private ConnectionManagerMetricsView view;

	@BeforeEach
	void setUp() {
		metrics = new ConnectionManagerMetrics();
		view = metrics.view();
	}

	@Test
	void construction() {
		// GIVEN + WHEN + THEN
		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void reset() {
		// GIVEN
		metrics.incrementConnectionCreateFailedCount();
		metrics.incrementPendingConnectionFailedCount();
		metrics.incrementPendingConnectionNotFoundCount();
		metrics.incrementIncomingConnectionNotFoundCount();
		metrics.incrementOutgoingConnectionNotFoundCount();
		metrics.incrementIncomingConnectionClosedCount();
		metrics.incrementOutgoingConnectionClosedCount();
		metrics.incrementPendingOutgoingConnectionCount();

		// WHEN
		metrics.reset();

		// THEN
		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void connectionCreateFailedCount() {
		// GIVEN + WHEN
		metrics.incrementConnectionCreateFailedCount();

		// THEN
		assertThat(view.connectionCreateFailedCount()).isEqualTo(1);

		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void pendingConnectionFailedCount() {
		// GIVEN + WHEN
		metrics.incrementPendingConnectionFailedCount();

		// THEN
		assertThat(view.pendingConnectionFailedCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void pendingConnectionNotFoundCount() {
		// GIVEN + WHEN
		metrics.incrementPendingConnectionNotFoundCount();

		// THEN
		assertThat(view.pendingConnectionNotFoundCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void incomingConnectionNotFoundCount() {
		// GIVEN + WHEN
		metrics.incrementIncomingConnectionNotFoundCount();

		// THEN
		assertThat(view.incomingConnectionNotFoundCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void outgoingConnectionNotFoundCount() {
		// GIVEN + WHEN
		metrics.incrementOutgoingConnectionNotFoundCount();

		// THEN
		assertThat(view.outgoingConnectionNotFoundCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void incomingConnectionClosedCount() {
		// GIVEN + WHEN
		metrics.incrementIncomingConnectionClosedCount();

		// THEN
		assertThat(view.incomingConnectionClosedCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void outgoingConnectionClosedCount() {
		// GIVEN + WHEN
		metrics.incrementOutgoingConnectionClosedCount();

		// THEN
		assertThat(view.outgoingConnectionClosedCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.pendingOutgoingConnectionCount()).isZero();
	}

	@Test
	void decrementPendingOutgoingConnectionCount() {
		// GIVEN
		metrics.incrementPendingOutgoingConnectionCount();
		metrics.incrementPendingOutgoingConnectionCount();

		// WHEN
		metrics.decrementPendingOutgoingConnectionCount();

		// THEN
		assertThat(view.pendingOutgoingConnectionCount()).isEqualTo(1);

		assertThat(view.connectionCreateFailedCount()).isZero();
		assertThat(view.pendingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionNotFoundCount()).isZero();
		assertThat(view.outgoingConnectionNotFoundCount()).isZero();
		assertThat(view.incomingConnectionClosedCount()).isZero();
		assertThat(view.outgoingConnectionClosedCount()).isZero();
	}
}
