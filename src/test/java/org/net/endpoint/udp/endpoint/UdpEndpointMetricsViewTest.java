package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UdpEndpointMetricsViewTest {

	private UdpEndpointMetrics metric;
	private UdpEndpointMetricsView view;

	@BeforeEach
	void setUp() {
		metric = new UdpEndpointMetrics();
		view = metric.view();
	}

	@Test
	void duplicateCreateConnectionCount_reflectsMetric() {
		// GIVEN
		metric.incrementDuplicateCreateConnectionCount();

		// WHEN
		long value = view.duplicateCreateConnectionCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void unknownPacketCount_reflectsMetric() {
		// GIVEN
		metric.incrementUnknownPacketCount();

		// WHEN
		long value = view.unknownPacketCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void unknownBytes_reflectsMetric() {
		// GIVEN
		metric.addUnknownBytes(123);

		// WHEN
		long value = view.unknownBytes();

		// THEN
		assertThat(value).isEqualTo(123);
	}

	@Test
	void incomingPacketCount_reflectsMetric() {
		// GIVEN
		metric.incrementIncomingPacketCount();

		// WHEN
		long value = view.incomingPacketCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void incomingBytes_reflectsMetric() {
		// GIVEN
		metric.addIncomingBytes(456);

		// WHEN
		long value = view.incomingBytes();

		// THEN
		assertThat(value).isEqualTo(456);
	}

	@Test
	void outgoingPacketCount_reflectsMetric() {
		// GIVEN
		metric.incrementOutgoingPacketCount();

		// WHEN
		long value = view.outgoingPacketCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void outgoingBytes_reflectsMetric() {
		// GIVEN
		metric.addOutgoingBytes(789);

		// WHEN
		long value = view.outgoingBytes();

		// THEN
		assertThat(value).isEqualTo(789);
	}

	@Test
	void dataCount_reflectsMetric() {
		// GIVEN
		metric.incrementDataCount();

		// WHEN
		long value = view.dataCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void heartbeatCount_reflectsMetric() {
		// GIVEN
		metric.incrementHeartbeatCount();

		// WHEN
		long value = view.heartbeatCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void createConnectionCount_reflectsMetric() {
		// GIVEN
		metric.incrementCreateConnectionCount();

		// WHEN
		long value = view.createConnectionCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionAcceptedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionAcceptedCount();

		// WHEN
		long value = view.connectionAcceptedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionRejectedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionRejectedCount();

		// WHEN
		long value = view.connectionRejectedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionClosedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionClosedCount();

		// WHEN
		long value = view.connectionClosedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void dataDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementDataDroppedCount();

		// WHEN
		long value = view.dataDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void heartbeatDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementHeartbeatDroppedCount();

		// WHEN
		long value = view.heartbeatDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void createConnectionDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementCreateConnectionDroppedCount();

		// WHEN
		long value = view.createConnectionDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionAcceptDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionAcceptDroppedCount();

		// WHEN
		long value = view.connectionAcceptDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionRejectDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionRejectDroppedCount();

		// WHEN
		long value = view.connectionRejectDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}

	@Test
	void connectionCloseDroppedCount_reflectsMetric() {
		// GIVEN
		metric.incrementConnectionCloseDroppedCount();

		// WHEN
		long value = view.connectionCloseDroppedCount();

		// THEN
		assertThat(value).isEqualTo(1);
	}
}
