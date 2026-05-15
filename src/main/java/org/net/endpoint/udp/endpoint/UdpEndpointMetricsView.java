package org.net.endpoint.udp.endpoint;

import lombok.RequiredArgsConstructor;
import org.net.endpoint.MetricsView;

@RequiredArgsConstructor
public final class UdpEndpointMetricsView implements MetricsView {
	private final UdpEndpointMetrics metrics;

	public long unknownPacketCount() {
		return metrics.unknownPacketCount();
	}

	public long unknownBytes() {
		return metrics.unknownBytes();
	}

	public long incomingPacketCount() {
		return metrics.incomingPacketCount();
	}

	public long incomingBytes() {
		return metrics.incomingBytes();
	}

	public long outgoingPacketCount() {
		return metrics.outgoingPacketCount();
	}

	public long outgoingBytes() {
		return metrics.outgoingBytes();
	}

	public long dataCount() {
		return metrics.dataCount();
	}

	public long heartbeatCount() {
		return metrics.heartbeatCount();
	}

	public long createConnectionCount() {
		return metrics.createConnectionCount();
	}

	public long connectionAcceptedCount() {
		return metrics.connectionAcceptedCount();
	}

	public long connectionRejectedCount() {
		return metrics.connectionRejectedCount();
	}

	public long connectionClosedCount() {
		return metrics.connectionClosedCount();
	}

	public long dataDroppedCount() {
		return metrics.dataDroppedCount();
	}

	public long heartbeatDroppedCount() {
		return metrics.heartbeatDroppedCount();
	}

	public long createConnectionDroppedCount() {
		return metrics.createConnectionDroppedCount();
	}

	public long connectionAcceptDroppedCount() {
		return metrics.connectionAcceptDroppedCount();
	}

	public long connectionRejectDroppedCount() {
		return metrics.connectionRejectDroppedCount();
	}

	public long connectionCloseDroppedCount() {
		return metrics.connectionCloseDroppedCount();
	}

	public long duplicateCreateConnectionCount() {
		return metrics.duplicateCreateConnectionCount();
	}
}

