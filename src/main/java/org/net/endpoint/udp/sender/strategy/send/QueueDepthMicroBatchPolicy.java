package org.net.endpoint.udp.sender.strategy.send;

public class QueueDepthMicroBatchPolicy implements BatchPolicy {

	@Override
	public int batchSize() {
		return 6;
	}

	@Override
	public long calculateTimeWindow(int queueSize) {
		if (queueSize < 0) throw new IllegalArgumentException("queueSize can't be negative!");
		if (queueSize <= 2) return 20_000L;
		if (queueSize <= 4) return 35_000L;
		if (queueSize <= 8) return 50_000L;
		if (queueSize <= 16) return 75_000L;
		return 100_000L;
	}
}
