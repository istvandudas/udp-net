package org.net.endpoint.udp.sender.strategy.send;

public interface BatchPolicy {
	int batchSize();
	long calculateTimeWindow(int queueSize);
}
