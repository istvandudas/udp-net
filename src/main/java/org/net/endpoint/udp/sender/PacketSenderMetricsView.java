package org.net.endpoint.udp.sender;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.net.endpoint.MetricsView;

@RequiredArgsConstructor
public class PacketSenderMetricsView implements MetricsView {
	private final @NonNull PacketSenderMetrics metrics;

	public long packetSentCount() {
		return metrics.packetSentCount();
	}

	public long packetSentPercentile(double percent) {
		return metrics.packetSentPercentile(percent);
	}

	public long enqueuedPacketCount() {
		return metrics.enqueuedPacketCount();
	}

	public long unsentPacketCount() {
		return metrics.unsentPacketCount();
	}

	public long partiallySentPacketCount() {
		return metrics.partiallySentPacketCount();
	}

	public long batchSizeCount() {
		return metrics.batchBytesCount();
	}

	public long batchSizePercentile(double percent) {
		return metrics.batchSizePercentile(percent);
	}

	public long sentBytes() {
		return metrics.sentBytes();
	}

	public long droppedBytes() {
		return metrics.droppedBytes();
	}

	public long errorCount() {
		return metrics.errorCount();
	}

	public long queueDepthCount() {
		return metrics.queueDepthCount();
	}

	public long queueDepthPercentile(double percent) {
		return metrics.queueDepthPercentile(percent);
	}

}
