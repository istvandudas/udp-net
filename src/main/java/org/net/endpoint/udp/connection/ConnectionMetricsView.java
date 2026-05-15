package org.net.endpoint.udp.connection;

import lombok.RequiredArgsConstructor;
import org.net.endpoint.MetricsView;

@RequiredArgsConstructor
public class ConnectionMetricsView implements MetricsView {
	private final ConnectionMetrics metrics;

	public long lastSentTime() {
		return metrics.lastSentTime();
	}

	public long lastReceivedTime() {
		return metrics.lastReceivedTime();
	}

	public long sentPacketCount() {
		return metrics.sentPacketCount();
	}

	public long sentBytes() {
		return metrics.sentBytes();
	}

	public long receivedPacketCount() {
		return metrics.receivedPacketCount();
	}

	public long receivedBytes() {
		return metrics.receivedBytes();
	}

	public long sentHeartbeatCount() {
		return metrics.sentHeartbeatCount();
	}

	public long receivedHeartbeatCount() {
		return metrics.receivedHeartbeatCount();
	}

	public long lastReceivedHeartbeatTime() {
		return metrics.lastReceivedHeartbeatTime();
	}
}
