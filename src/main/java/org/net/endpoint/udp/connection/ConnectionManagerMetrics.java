package org.net.endpoint.udp.connection;

import org.net.endpoint.Metrics;

import java.util.concurrent.atomic.AtomicLong;

public class ConnectionManagerMetrics implements Metrics {

	private final ConnectionManagerMetricsView view = new ConnectionManagerMetricsView(this);

	private final AtomicLong connectionCreateFailedCount = new AtomicLong(0L);
	private final AtomicLong pendingConnectionFailedCount = new AtomicLong(0L);
	private final AtomicLong pendingConnectionNotFoundCount = new AtomicLong(0L);
	private final AtomicLong incomingConnectionNotFoundCount = new AtomicLong(0L);
	private final AtomicLong outgoingConnectionNotFoundCount = new AtomicLong(0L);
	private final AtomicLong incomingConnectionClosedCount = new AtomicLong(0L);
	private final AtomicLong outgoingConnectionClosedCount = new AtomicLong(0L);
	private final AtomicLong pendingOutgoingConnectionCount = new AtomicLong(0L);

	@Override
	public ConnectionManagerMetricsView view() {
		return view;
	}

	@Override
	public void reset() {
		connectionCreateFailedCount.set(0L);
		pendingConnectionFailedCount.set(0L);
		pendingConnectionNotFoundCount.set(0L);
		incomingConnectionNotFoundCount.set(0L);
		outgoingConnectionNotFoundCount.set(0L);
		incomingConnectionClosedCount.set(0L);
		outgoingConnectionClosedCount.set(0L);
		pendingOutgoingConnectionCount.set(0L);
	}

	public void incrementPendingOutgoingConnectionCount() {
		pendingOutgoingConnectionCount.incrementAndGet();
	}

	public void decrementPendingOutgoingConnectionCount() {
		pendingOutgoingConnectionCount.decrementAndGet();
	}

	public void incrementConnectionCreateFailedCount() {
		connectionCreateFailedCount.incrementAndGet();
	}

	public void incrementPendingConnectionFailedCount() {
		pendingConnectionFailedCount.incrementAndGet();
	}

	public void incrementPendingConnectionNotFoundCount() {
		pendingConnectionNotFoundCount.incrementAndGet();
	}

	public void incrementIncomingConnectionNotFoundCount() {
		incomingConnectionNotFoundCount.incrementAndGet();
	}

	public void incrementOutgoingConnectionNotFoundCount() {
		outgoingConnectionNotFoundCount.incrementAndGet();
	}

	public void incrementIncomingConnectionClosedCount() {
		incomingConnectionClosedCount.incrementAndGet();
	}

	public void incrementOutgoingConnectionClosedCount() {
		outgoingConnectionClosedCount.incrementAndGet();
	}

	long connectionCreateFailedCount() {
		return connectionCreateFailedCount.get();
	}

	long pendingConnectionFailedCount() {
		return pendingConnectionFailedCount.get();
	}

	long pendingConnectionNotFoundCount() {
		return pendingConnectionNotFoundCount.get();
	}

	long incomingConnectionNotFoundCount() {
		return incomingConnectionNotFoundCount.get();
	}

	long outgoingConnectionNotFoundCount() {
		return outgoingConnectionNotFoundCount.get();
	}

	long incomingConnectionClosedCount() {
		return incomingConnectionClosedCount.get();
	}

	long outgoingConnectionClosedCount() {
		return outgoingConnectionClosedCount.get();
	}

	long pendingOutgoingConnectionCount() {
		return pendingOutgoingConnectionCount.get();
	}
}