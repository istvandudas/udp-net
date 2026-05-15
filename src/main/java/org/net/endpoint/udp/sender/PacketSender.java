package org.net.endpoint.udp.sender;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jctools.queues.varhandle.MpscVarHandleArrayQueue;
import org.net.endpoint.common.NetService;
import org.net.endpoint.udp.sender.strategy.send.SendStrategy;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Queue;

@Slf4j
public class PacketSender extends NetService implements Runnable {
	public static final int DEFAULT_QUEUE_CAPACITY = 2048;
	private static final String NAME_POSTFIX = ".sender";

	private final Queue<SendRequest> queue;
	private final PacketSenderMetrics metrics;
	private final SendStrategy<SendRequest> sendStrategy;


	public PacketSender(String name, PacketSenderMetrics metrics, SendStrategy<SendRequest> sendStrategy) {
		this(name, metrics, sendStrategy, null);
	}

	public PacketSender(@NonNull String name,
						@NonNull PacketSenderMetrics metrics,
						@NonNull SendStrategy<SendRequest> sendStrategy,
						Queue<SendRequest> queue
	) {
		super(name + NAME_POSTFIX, false);
		this.queue = Objects.requireNonNullElseGet(queue, () -> new MpscVarHandleArrayQueue<>(DEFAULT_QUEUE_CAPACITY));
		this.metrics = metrics;
		this.sendStrategy = sendStrategy;
	}

	public PacketSenderMetricsView metrics() {
		return metrics.view();
	}

	public boolean send(@NonNull ByteBuffer buffer, @NonNull SocketAddress target, boolean release) {
		SendRequest request = SendRequest.create();
		if (request == null) {
			return false;
		}
		request.set(buffer, target, release);
		if (queue.offer(request)) {
			metrics.incrementEnqueuedPacketCount();
			return true;
		}
		else {
			request.release();
			return false;
		}
	}

	@Override
	public void run() {
		while (running.get()) {
			sendStrategy.send(queue);
			metrics.recordQueueDepth(queue.size());
		}
	}

}