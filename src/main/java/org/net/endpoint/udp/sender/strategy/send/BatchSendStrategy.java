package org.net.endpoint.udp.sender.strategy.send;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.sender.PacketSenderMetrics;
import org.net.endpoint.udp.sender.SendRequest;
import org.net.endpoint.udp.sender.strategy.poll.PollStrategy;
import org.net.endpoint.udp.sender.strategy.poll.ThreePhaseWaitStrategy;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.Queue;

@Slf4j
public class BatchSendStrategy implements SendStrategy<SendRequest> {
	private final BatchPolicy policy;
	private final PollStrategy<SendRequest> pollStrategy;
	private final TimeMachine timeMachine;
	private final DatagramChannel channel;
	private final PacketSenderMetrics metrics;
	private final SendRequest[] batch;
	private final int maxPollCount;

	private final BufferPool bufferPool;

	public BatchSendStrategy(
			DatagramChannel channel,
			TimeMachine timeMachine,
			PacketSenderMetrics metrics,
			BufferPool bufferPool
	) {
		this(
				new QueueDepthMicroBatchPolicy(),
				new ThreePhaseWaitStrategy<>(),
				channel,
				timeMachine,
				metrics,
				bufferPool
		);
	}

	public BatchSendStrategy(
			BatchPolicy policy,
			PollStrategy<SendRequest> pollStrategy,
			DatagramChannel channel,
			TimeMachine timeMachine,
			PacketSenderMetrics metrics,
			BufferPool bufferPool
	) {
		this.policy = policy;
		this.pollStrategy = pollStrategy;
		this.timeMachine = timeMachine;
		this.channel = channel;
		this.metrics = metrics;
		this.bufferPool = bufferPool;
		maxPollCount = policy.batchSize();
		batch = new SendRequest[maxPollCount];
	}

	@Override
	public int send(@NonNull Queue<SendRequest> queue) {
		SendRequest request = pollStrategy.poll(queue);
		if (request == null) {
			return 0;
		}
		long start = timeMachine.nanoNow();
		batch[0] = request;
		int pollCount = 1;
		long microTimeWindow = policy.calculateTimeWindow(queue.size());
		while (timeMachine.nanoElapsed(start) < microTimeWindow && pollCount < maxPollCount) {
			request = queue.poll();
			if (request != null) {
				batch[pollCount++] = request;
			}
		}
		int sentBytes;
		int batchBytes = 0;
		for (int i = 0; i < pollCount; i++) {
			SendRequest req = batch[i];
			if (req == null) continue;
			long startSending = timeMachine.nanoNow();
			int contentSize = req.writeableSize();
			try {
				req.getBuffer().position(0);
				sentBytes = channel.send(req.getBuffer(), req.getTarget());
				metrics.recordPacketSent(timeMachine.nanoElapsed(startSending));
				batchBytes += sentBytes;
				metrics.addSentBytes(sentBytes);
				if (sentBytes > 0) {
					if (sentBytes != contentSize) {
						metrics.incrementPartiallySentPacketCount();
						metrics.addDroppedBytes(contentSize - sentBytes);
					}
				} else {
					metrics.incrementUnsentPacketCount();
					metrics.addDroppedBytes(contentSize);
				}
			} catch (IOException e) {
				metrics.incrementErrorCount();
			} finally {
				if (req.isRelease()) {
					bufferPool.release(req.getBuffer());
				}
				req.release();
			}
		}
		metrics.recordBatchBytes(batchBytes);
		return pollCount;
	}
}
