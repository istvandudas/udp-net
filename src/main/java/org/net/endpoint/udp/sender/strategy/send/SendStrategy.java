package org.net.endpoint.udp.sender.strategy.send;

import java.util.Queue;

public interface SendStrategy<T> {
	int send(Queue<T> queue);
}
