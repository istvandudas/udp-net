package org.net.endpoint.udp.connection;

import org.net.endpoint.Metrics;

import java.util.concurrent.atomic.AtomicLong;

public class ConnectionMetrics implements Metrics {

	private final ConnectionMetricsView view = new ConnectionMetricsView(this);

	private final AtomicLong lastSentTime = new AtomicLong(0L);
	private final AtomicLong lastReceivedTime = new AtomicLong(0L);
	private final AtomicLong sentPacketCount = new AtomicLong(0L);
	private final AtomicLong sentBytes = new AtomicLong(0L);
	private final AtomicLong receivedPacketCount = new AtomicLong(0L);
	private final AtomicLong receivedBytes = new AtomicLong(0L);

	private final AtomicLong sentHeartbeatCount = new AtomicLong(0L);
	private final AtomicLong receivedHeartbeatCount = new AtomicLong(0L);
	private final AtomicLong lastReceivedHeartbeatTime = new AtomicLong(0L);

	@Override
	public ConnectionMetricsView view() {
		return view;
	}

	public void reset() {
		lastSentTime.set(0L);
		lastReceivedTime.set(0L);
		sentPacketCount.set(0L);
		sentBytes.set(0L);
		receivedPacketCount.set(0L);
		receivedBytes.set(0L);
		sentHeartbeatCount.set(0L);
		receivedHeartbeatCount.set(0L);
		lastReceivedHeartbeatTime.set(0L);
	}

	public long lastSentTime() {
		return lastSentTime.get();
	}

	public void lastSentTime(long time) {
		lastSentTime.getAndSet(time);
	}

	public long lastReceivedTime() {
		return lastReceivedTime.get();
	}

	public void lastReceivedTime(long time) {
		lastReceivedTime.set(time);
	}

	public long sentPacketCount() {
		return sentPacketCount.get();
	}

	public void incrementSentPacketCount() {
		sentPacketCount.incrementAndGet();
	}

	public long sentBytes() {
		return sentBytes.get();
	}

	public void addSentBytes(long bytes) {
		sentBytes.addAndGet(bytes);
	}

	public long receivedPacketCount() {
		return receivedPacketCount.get();
	}

	public void incrementReceivedPacketCount() {
		receivedPacketCount.incrementAndGet();
	}

	public long receivedBytes() {
		return receivedBytes.get();
	}

	public void addReceivedBytes(long bytes) {
		receivedBytes.addAndGet(bytes);
	}

	public long sentHeartbeatCount() {
		return sentHeartbeatCount.get();
	}

	public void incrementSentHeartbeatCount() {
		sentHeartbeatCount.incrementAndGet();
	}

	public long receivedHeartbeatCount() {
		return receivedHeartbeatCount.get();
	}

	public void incrementReceivedHeartbeatCount() {
		receivedHeartbeatCount.incrementAndGet();
	}

	public long lastReceivedHeartbeatTime() {
		return lastReceivedHeartbeatTime.get();
	}

	public void lastReceivedHeartbeatTime(long time) {
		lastReceivedHeartbeatTime.set(time);
	}

}
