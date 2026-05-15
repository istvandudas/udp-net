package org.net.endpoint.udp.sender;

import org.HdrHistogram.Histogram;
import org.net.endpoint.Metrics;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class PacketSenderMetrics implements Metrics {

	public static final long DEFAULT_HISTOGRAM_MAX_SENT_TIME = Duration.ofMillis(5).toNanos();
	public static final long DEFAULT_HISTOGRAM_MAX_BATCH_SIZE = 8;

	public static final long LOWEST_DISCERNIBLE_VALUE = 1L;
	public static final int NUMBER_OF_SIGNIFICANT_DIGITS = 4;

	private final PacketSenderMetricsView view = new PacketSenderMetricsView(this);

	private final Histogram packetSentHistogram;
	private final AtomicLong enqueuedPacketCount = new AtomicLong(0L);
	private final AtomicLong unsentPacketCount = new AtomicLong(0L);
	private final AtomicLong partiallySentPacketCount = new AtomicLong(0L);
	private final Histogram batchBytesHistogram;
	private final AtomicLong sentBytes = new AtomicLong(0L);
	private final AtomicLong droppedBytes = new AtomicLong(0L);
	private final AtomicLong errorCount = new AtomicLong(0L);
	private final Histogram queueDepthHistogram;

	public PacketSenderMetrics() {
		this(
				DEFAULT_HISTOGRAM_MAX_SENT_TIME,
				DEFAULT_HISTOGRAM_MAX_BATCH_SIZE,
				PacketSender.DEFAULT_QUEUE_CAPACITY
		);
	}

	public PacketSenderMetrics(long histogramMaxSentTime, long histogramMaxBatchSize, long histogramMaxQueueDepth) {
		if (histogramMaxSentTime <= LOWEST_DISCERNIBLE_VALUE)
			throw new IllegalArgumentException("histogramMaxSentTime must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
		if (histogramMaxBatchSize <= LOWEST_DISCERNIBLE_VALUE)
			throw new IllegalArgumentException("histogramMaxBatchSize must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
		if (histogramMaxQueueDepth <= LOWEST_DISCERNIBLE_VALUE)
			throw new IllegalArgumentException("histogramMaxQueueDepth must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
		packetSentHistogram = new Histogram(LOWEST_DISCERNIBLE_VALUE, histogramMaxSentTime, NUMBER_OF_SIGNIFICANT_DIGITS);
		batchBytesHistogram = new Histogram(LOWEST_DISCERNIBLE_VALUE, histogramMaxBatchSize, NUMBER_OF_SIGNIFICANT_DIGITS);
		queueDepthHistogram = new Histogram(LOWEST_DISCERNIBLE_VALUE, histogramMaxQueueDepth, NUMBER_OF_SIGNIFICANT_DIGITS);
	}

	public void recordPacketSent(long time) {
		packetSentHistogram.recordValue(Math.min(time, packetSentHistogram.getHighestTrackableValue()));
	}

	public long packetSentCount() {
		return packetSentHistogram.getTotalCount();
	}

	public long packetSentPercentile(double percentile) {
		return packetSentHistogram.getValueAtPercentile(percentile);
	}

	public long enqueuedPacketCount() {
		return enqueuedPacketCount.get();
	}

	public void incrementEnqueuedPacketCount() {
		enqueuedPacketCount.incrementAndGet();
	}

	public long unsentPacketCount() {
		return unsentPacketCount.get();
	}

	public void incrementUnsentPacketCount() {
		unsentPacketCount.getAndIncrement();
	}

	public long partiallySentPacketCount() {
		return partiallySentPacketCount.get();
	}

	public void incrementPartiallySentPacketCount() {
		partiallySentPacketCount.getAndIncrement();
	}

	public void recordBatchBytes(long size) {
		batchBytesHistogram.recordValue(size);
	}

	public long batchBytesCount() {
		return batchBytesHistogram.getTotalCount();
	}

	public long batchSizePercentile(double percent) {
		return batchBytesHistogram.getValueAtPercentile(percent);
	}

	public long sentBytes() {
		return sentBytes.longValue();
	}

	public void addSentBytes(long bytes) {
		sentBytes.addAndGet(bytes);
	}

	public long droppedBytes() {
		return droppedBytes.get();
	}

	public void addDroppedBytes(long delta) {
		droppedBytes.getAndAdd(delta);
	}

	public long errorCount() {
		return errorCount.longValue();
	}

	public void incrementErrorCount() {
		errorCount.getAndIncrement();
	}

	public void recordQueueDepth(long depth) {
		queueDepthHistogram.recordValue(depth);
	}

	public long queueDepthCount() {
		return queueDepthHistogram.getTotalCount();
	}

	public long queueDepthPercentile(double percent) {
		return queueDepthHistogram.getValueAtPercentile(percent);
	}

	@Override
	public PacketSenderMetricsView view() {
		return view;
	}

	@Override
	public void reset() {
		packetSentHistogram.reset();
		enqueuedPacketCount.set(0L);
		unsentPacketCount.set(0L);
		partiallySentPacketCount.set(0L);
		batchBytesHistogram.reset();
		sentBytes.set(0L);
		droppedBytes.set(0L);
		errorCount.set(0L);
		queueDepthHistogram.reset();
	}
}
