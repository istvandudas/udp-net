package org.net.endpoint.udp.connection;

import lombok.RequiredArgsConstructor;
import org.net.endpoint.MetricsView;

@RequiredArgsConstructor
public class ConnectionManagerMetricsView implements MetricsView {
	private final ConnectionManagerMetrics metrics;

	public long connectionCreateFailedCount() {
		return metrics.connectionCreateFailedCount();
	}

	public long pendingConnectionFailedCount() {
		return metrics.pendingConnectionFailedCount();
	}

	public long pendingConnectionNotFoundCount() {
		return metrics.pendingConnectionNotFoundCount();
	}

	public long incomingConnectionNotFoundCount() {
		return metrics.incomingConnectionNotFoundCount();
	}

	public long outgoingConnectionNotFoundCount() {
		return metrics.outgoingConnectionNotFoundCount();
	}

	public long incomingConnectionClosedCount() {
		return metrics.incomingConnectionClosedCount();
	}

	public long outgoingConnectionClosedCount() {
		return metrics.outgoingConnectionClosedCount();
	}

	public long pendingOutgoingConnectionCount() {
		return metrics.pendingOutgoingConnectionCount();
	}
}
