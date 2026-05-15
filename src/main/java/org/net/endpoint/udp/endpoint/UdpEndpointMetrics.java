package org.net.endpoint.udp.endpoint;

import org.net.endpoint.Metrics;

import java.util.concurrent.atomic.AtomicLong;

public final class UdpEndpointMetrics implements Metrics {

	private final AtomicLong unknownPacketCount = new AtomicLong();
	private final AtomicLong unknownBytes = new AtomicLong();

	private final AtomicLong incomingPacketCount = new AtomicLong();
	private final AtomicLong incomingBytes = new AtomicLong();

	private final AtomicLong outgoingPacketCount = new AtomicLong();
	private final AtomicLong outgoingBytes = new AtomicLong();

	private final AtomicLong dataCount = new AtomicLong();
	private final AtomicLong heartbeatCount = new AtomicLong();
	private final AtomicLong createConnectionCount = new AtomicLong();
	private final AtomicLong connectionAcceptedCount = new AtomicLong();
	private final AtomicLong connectionRejectedCount = new AtomicLong();
	private final AtomicLong connectionClosedCount = new AtomicLong();

	private final AtomicLong dataDroppedCount = new AtomicLong();
	private final AtomicLong heartbeatDroppedCount = new AtomicLong();
	private final AtomicLong createConnectionDroppedCount = new AtomicLong();
	private final AtomicLong connectionAcceptDroppedCount = new AtomicLong();
	private final AtomicLong connectionRejectDroppedCount = new AtomicLong();
	private final AtomicLong connectionCloseDroppedCount = new AtomicLong();

	private final AtomicLong duplicateCreateConnectionCount = new AtomicLong();

	private final UdpEndpointMetricsView view = new UdpEndpointMetricsView(this);

	@Override
	public UdpEndpointMetricsView view() {
		return view;
	}

	public void incrementDuplicateCreateConnectionCount() {
		duplicateCreateConnectionCount.incrementAndGet();
	}

	public long duplicateCreateConnectionCount() {
		return duplicateCreateConnectionCount.get();
	}

	public void incrementUnknownPacketCount() {
		unknownPacketCount.incrementAndGet();
	}

	public void addUnknownBytes(long bytes) {
		unknownBytes.addAndGet(bytes);
	}

	public void incrementIncomingPacketCount() {
		incomingPacketCount.incrementAndGet();
	}

	public void addIncomingBytes(long bytes) {
		incomingBytes.addAndGet(bytes);
	}

	public void incrementOutgoingPacketCount() {
		outgoingPacketCount.incrementAndGet();
	}

	public void addOutgoingBytes(long bytes) {
		outgoingBytes.addAndGet(bytes);
	}

	public void incrementDataCount() {
		dataCount.incrementAndGet();
	}

	public void incrementHeartbeatCount() {
		heartbeatCount.incrementAndGet();
	}

	public void incrementCreateConnectionCount() {
		createConnectionCount.incrementAndGet();
	}

	public void incrementConnectionAcceptedCount() {
		connectionAcceptedCount.incrementAndGet();
	}

	public void incrementConnectionRejectedCount() {
		connectionRejectedCount.incrementAndGet();
	}

	public void incrementConnectionClosedCount() {
		connectionClosedCount.incrementAndGet();
	}

	public void incrementDataDroppedCount() {
		dataDroppedCount.incrementAndGet();
	}

	public void incrementHeartbeatDroppedCount() {
		heartbeatDroppedCount.incrementAndGet();
	}

	public void incrementCreateConnectionDroppedCount() {
		createConnectionDroppedCount.incrementAndGet();
	}

	public void incrementConnectionAcceptDroppedCount() {
		connectionAcceptDroppedCount.incrementAndGet();
	}

	public void incrementConnectionRejectDroppedCount() {
		connectionRejectDroppedCount.incrementAndGet();
	}

	public void incrementConnectionCloseDroppedCount() {
		connectionCloseDroppedCount.incrementAndGet();
	}

	@Override
	public void reset() {
		unknownPacketCount.set(0);
		unknownBytes.set(0);

		incomingPacketCount.set(0);
		incomingBytes.set(0);

		outgoingPacketCount.set(0);
		outgoingBytes.set(0);

		dataCount.set(0);
		heartbeatCount.set(0);
		createConnectionCount.set(0);
		connectionAcceptedCount.set(0);
		connectionRejectedCount.set(0);
		connectionClosedCount.set(0);

		dataDroppedCount.set(0);
		heartbeatDroppedCount.set(0);
		createConnectionDroppedCount.set(0);
		connectionAcceptDroppedCount.set(0);
		connectionRejectDroppedCount.set(0);
		connectionCloseDroppedCount.set(0);
	}

	long unknownPacketCount() {
		return unknownPacketCount.get();
	}

	long unknownBytes() {
		return unknownBytes.get();
	}

	long incomingPacketCount() {
		return incomingPacketCount.get();
	}

	long incomingBytes() {
		return incomingBytes.get();
	}

	long outgoingPacketCount() {
		return outgoingPacketCount.get();
	}

	long outgoingBytes() {
		return outgoingBytes.get();
	}

	long dataCount() {
		return dataCount.get();
	}

	long heartbeatCount() {
		return heartbeatCount.get();
	}

	long createConnectionCount() {
		return createConnectionCount.get();
	}

	long connectionAcceptedCount() {
		return connectionAcceptedCount.get();
	}

	long connectionRejectedCount() {
		return connectionRejectedCount.get();
	}

	long connectionClosedCount() {
		return connectionClosedCount.get();
	}

	long dataDroppedCount() {
		return dataDroppedCount.get();
	}

	long heartbeatDroppedCount() {
		return heartbeatDroppedCount.get();
	}

	long createConnectionDroppedCount() {
		return createConnectionDroppedCount.get();
	}

	long connectionAcceptDroppedCount() {
		return connectionAcceptDroppedCount.get();
	}

	long connectionRejectDroppedCount() {
		return connectionRejectDroppedCount.get();
	}

	long connectionCloseDroppedCount() {
		return connectionCloseDroppedCount.get();
	}
}
