package org.net.endpoint.udp.sender.strategy.poll;

import java.util.Queue;

public interface PollStrategy<T> {
	T poll(Queue<T> queue);
}
