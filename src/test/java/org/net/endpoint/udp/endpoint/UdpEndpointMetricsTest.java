package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UdpEndpointMetricsTest {

	private UdpEndpointMetrics newMetric() {
		return new UdpEndpointMetrics();
	}

	@Test
	void incrementDuplicateCreateConnectionCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementDuplicateCreateConnectionCount();

		// THEN
		assertThat(metric.view().duplicateCreateConnectionCount()).isEqualTo(1);
		assertAllOthersZero(metric, "duplicateCreateConnectionCount");
	}


	@Test
	void incrementUnknownPacketCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementUnknownPacketCount();

		// THEN
		assertThat(metric.view().unknownPacketCount()).isEqualTo(1);
		assertAllOthersZero(metric, "unknownPacketCount");
	}

	@Test
	void setUnknownBytes_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.addUnknownBytes(123);

		// THEN
		assertThat(metric.view().unknownBytes()).isEqualTo(123);
		assertAllOthersZero(metric, "unknownBytes");
	}

	@Test
	void incrementIncomingPacketCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementIncomingPacketCount();

		// THEN
		assertThat(metric.view().incomingPacketCount()).isEqualTo(1);
		assertAllOthersZero(metric, "incomingPacketCount");
	}

	@Test
	void setIncomingBytes_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.addIncomingBytes(456);

		// THEN
		assertThat(metric.view().incomingBytes()).isEqualTo(456);
		assertAllOthersZero(metric, "incomingBytes");
	}

	@Test
	void incrementOutgoingPacketCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementOutgoingPacketCount();

		// THEN
		assertThat(metric.view().outgoingPacketCount()).isEqualTo(1);
		assertAllOthersZero(metric, "outgoingPacketCount");
	}

	@Test
	void addOutgoingBytes_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.addOutgoingBytes(789);

		// THEN
		assertThat(metric.view().outgoingBytes()).isEqualTo(789);
		assertAllOthersZero(metric, "outgoingBytes");
	}

	@Test
	void incrementDataCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementDataCount();

		// THEN
		assertThat(metric.view().dataCount()).isEqualTo(1);
		assertAllOthersZero(metric, "dataCount");
	}

	@Test
	void incrementHeartbeatCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementHeartbeatCount();

		// THEN
		assertThat(metric.view().heartbeatCount()).isEqualTo(1);
		assertAllOthersZero(metric, "heartbeatCount");
	}

	@Test
	void incrementCreateConnectionCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementCreateConnectionCount();

		// THEN
		assertThat(metric.view().createConnectionCount()).isEqualTo(1);
		assertAllOthersZero(metric, "createConnectionCount");
	}

	@Test
	void incrementConnectionAcceptedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionAcceptedCount();

		// THEN
		assertThat(metric.view().connectionAcceptedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionAcceptedCount");
	}

	@Test
	void incrementConnectionRejectedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionRejectedCount();

		// THEN
		assertThat(metric.view().connectionRejectedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionRejectedCount");
	}

	@Test
	void incrementConnectionClosedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionClosedCount();

		// THEN
		assertThat(metric.view().connectionClosedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionClosedCount");
	}

	@Test
	void incrementDataDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementDataDroppedCount();

		// THEN
		assertThat(metric.view().dataDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "dataDroppedCount");
	}

	@Test
	void incrementHeartbeatDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementHeartbeatDroppedCount();

		// THEN
		assertThat(metric.view().heartbeatDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "heartbeatDroppedCount");
	}

	@Test
	void incrementCreateConnectionDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementCreateConnectionDroppedCount();

		// THEN
		assertThat(metric.view().createConnectionDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "createConnectionDroppedCount");
	}

	@Test
	void incrementConnectionAcceptDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionAcceptDroppedCount();

		// THEN
		assertThat(metric.view().connectionAcceptDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionAcceptDroppedCount");
	}

	@Test
	void incrementConnectionRejectDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionRejectDroppedCount();

		// THEN
		assertThat(metric.view().connectionRejectDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionRejectDroppedCount");
	}

	@Test
	void incrementConnectionCloseDroppedCount_updatesOnlyThatMetric() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();

		// WHEN
		metric.incrementConnectionCloseDroppedCount();

		// THEN
		assertThat(metric.view().connectionCloseDroppedCount()).isEqualTo(1);
		assertAllOthersZero(metric, "connectionCloseDroppedCount");
	}

	@Test
	void reset_clearsAllMetrics() {
		// GIVEN
		UdpEndpointMetrics metric = newMetric();
		metric.incrementIncomingPacketCount();
		metric.addOutgoingBytes(999);

		// WHEN
		metric.reset();

		// THEN
		assertAllZero(metric);
	}

	private void assertAllZero(UdpEndpointMetrics metric) {
		assertThat(metric.view().unknownPacketCount()).isZero();
		assertThat(metric.view().unknownBytes()).isZero();

		assertThat(metric.view().incomingPacketCount()).isZero();
		assertThat(metric.view().incomingBytes()).isZero();

		assertThat(metric.view().outgoingPacketCount()).isZero();
		assertThat(metric.view().outgoingBytes()).isZero();

		assertThat(metric.view().dataCount()).isZero();
		assertThat(metric.view().heartbeatCount()).isZero();
		assertThat(metric.view().createConnectionCount()).isZero();
		assertThat(metric.view().connectionAcceptedCount()).isZero();
		assertThat(metric.view().connectionRejectedCount()).isZero();
		assertThat(metric.view().connectionClosedCount()).isZero();

		assertThat(metric.view().dataDroppedCount()).isZero();
		assertThat(metric.view().heartbeatDroppedCount()).isZero();
		assertThat(metric.view().createConnectionDroppedCount()).isZero();
		assertThat(metric.view().connectionAcceptDroppedCount()).isZero();
		assertThat(metric.view().connectionRejectDroppedCount()).isZero();
		assertThat(metric.view().connectionCloseDroppedCount()).isZero();
	}

	private void assertAllOthersZero(UdpEndpointMetrics metric, String except) {
		if (!except.equals("unknownPacketCount")) assertThat(metric.view().unknownPacketCount()).isZero();
		if (!except.equals("unknownBytes")) assertThat(metric.view().unknownBytes()).isZero();

		if (!except.equals("incomingPacketCount")) assertThat(metric.view().incomingPacketCount()).isZero();
		if (!except.equals("incomingBytes")) assertThat(metric.view().incomingBytes()).isZero();

		if (!except.equals("outgoingPacketCount")) assertThat(metric.view().outgoingPacketCount()).isZero();
		if (!except.equals("outgoingBytes")) assertThat(metric.view().outgoingBytes()).isZero();

		if (!except.equals("dataCount")) assertThat(metric.view().dataCount()).isZero();
		if (!except.equals("heartbeatCount")) assertThat(metric.view().heartbeatCount()).isZero();
		if (!except.equals("createConnectionCount")) assertThat(metric.view().createConnectionCount()).isZero();
		if (!except.equals("connectionAcceptedCount")) assertThat(metric.view().connectionAcceptedCount()).isZero();
		if (!except.equals("connectionRejectedCount")) assertThat(metric.view().connectionRejectedCount()).isZero();
		if (!except.equals("connectionClosedCount")) assertThat(metric.view().connectionClosedCount()).isZero();

		if (!except.equals("dataDroppedCount")) assertThat(metric.view().dataDroppedCount()).isZero();
		if (!except.equals("heartbeatDroppedCount")) assertThat(metric.view().heartbeatDroppedCount()).isZero();
		if (!except.equals("createConnectionDroppedCount"))
			assertThat(metric.view().createConnectionDroppedCount()).isZero();
		if (!except.equals("connectionAcceptDroppedCount"))
			assertThat(metric.view().connectionAcceptDroppedCount()).isZero();
		if (!except.equals("connectionRejectDroppedCount"))
			assertThat(metric.view().connectionRejectDroppedCount()).isZero();
		if (!except.equals("connectionCloseDroppedCount"))
			assertThat(metric.view().connectionCloseDroppedCount()).isZero();
		if (!except.equals("duplicateCreateConnectionCount"))
			assertThat(metric.view().duplicateCreateConnectionCount()).isZero();

	}
}