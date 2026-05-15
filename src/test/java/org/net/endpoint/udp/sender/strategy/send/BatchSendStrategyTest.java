package org.net.endpoint.udp.sender.strategy.send;

import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.sender.PacketSenderMetrics;
import org.net.endpoint.udp.sender.SendRequest;
import org.net.endpoint.udp.sender.strategy.poll.PollStrategy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked", "DataFlowIssue"})
class BatchSendStrategyTest {

	private static final int BATCH_SIZE = 6;

	// the test subject
	private BatchSendStrategy strategy;

	private DatagramChannel channel;
	private TimeMachine timeMachine;
	private PacketSenderMetrics metrics;
	private BatchPolicy batchPolicy;
	private PollStrategy<SendRequest> pollStrategy;

	private ArgumentCaptor<Long> sentBytesCaptor;
	private ArgumentCaptor<Long> metricsBytesCaptor;
	private BufferPool bufferPool;

	private SendRequest request1;
	private SendRequest request2;
	private Queue<SendRequest> queue;

	@BeforeEach
	void setUp() {
		channel = mock(DatagramChannel.class);
		timeMachine = mock(TimeMachine.class);
		metrics = mock(PacketSenderMetrics.class);
		batchPolicy = mock(BatchPolicy.class);
		pollStrategy = mock(PollStrategy.class);
		sentBytesCaptor = ArgumentCaptor.captor();
		metricsBytesCaptor = ArgumentCaptor.captor();
		bufferPool = mock(BufferPool.class);
	}

	@Test
	void send_queue_isNull() {
		// GIVEN
		strategy = givenStrategy();

		// WHEN + THEN
		assertThatThrownBy(() -> strategy.send(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("queue is marked non-null but is null");
	}

	@Test
	void batchPolicy_batchSize_usedInConstructor() {
		// GIVEN
		strategy = givenStrategy();

		// WHEN + THEN
		verify(batchPolicy).batchSize();
	}

	@Test
	void send_whenQueueIsEmpty() {
		// GIVEN
		queue = mock(Queue.class);
		strategy = givenStrategy();
		given(pollStrategy.poll(queue)).willReturn(null);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isZero();
		verifyNoInteractions(timeMachine);
		verify(batchPolicy).batchSize();
		verifyNoInteractions(metrics);
		verify(pollStrategy).poll(queue);
	}

	@Test
	void send_noReleaseForOne() throws IOException {
		// GIVEN
		givenTwoRequestsInTheQueue(142, 39, 142, 39, true, false);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isEqualTo(2);

		verify(request1).release();
		verify(request2).release();
	}

	@Test
	void send_nothingSent() throws IOException {
		// GIVEN
		givenTwoRequestsInTheQueue(142, 39, 0, 0);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isEqualTo(2);

		verify(metrics, times(2)).addSentBytes(0);
		verify(metrics, never()).incrementPartiallySentPacketCount();
		verify(metrics, times(2)).incrementUnsentPacketCount();
		verify(metrics, times(2)).addDroppedBytes(metricsBytesCaptor.capture());
		assertThat(metricsBytesCaptor.getAllValues().get(0)).isEqualTo(142L);
		assertThat(metricsBytesCaptor.getAllValues().get(1)).isEqualTo(39L);
	}

	@Test
	void send_partial_one() throws IOException {
		// GIVEN
		givenTwoRequestsInTheQueue(142, 39, 140, 39);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isEqualTo(2);

		verify(metrics, times(2)).addSentBytes(sentBytesCaptor.capture());
		assertThat(sentBytesCaptor.getAllValues().get(0)).isEqualTo(140);
		assertThat(sentBytesCaptor.getAllValues().get(1)).isEqualTo(39);

		verify(metrics, never()).incrementUnsentPacketCount();
		verify(metrics).incrementPartiallySentPacketCount();
		verify(metrics).addDroppedBytes(2);
	}

	@Test
	void send_partial_all() throws IOException {
		// GIVEN
		givenTwoRequestsInTheQueue(142, 39, 140, 36);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isEqualTo(2);

		verify(metrics, times(2)).addSentBytes(sentBytesCaptor.capture());
		assertThat(sentBytesCaptor.getAllValues().get(0)).isEqualTo(140L);
		assertThat(sentBytesCaptor.getAllValues().get(1)).isEqualTo(36L);

		verify(metrics, never()).incrementUnsentPacketCount();
		verify(metrics, times(2)).incrementPartiallySentPacketCount();
		verify(metrics).addDroppedBytes(2);
		verify(metrics).addDroppedBytes(3);
	}

	@Test
	void send_throws_IOException() throws IOException {
		// GIVEN
		givenBatchPolicy(BATCH_SIZE);
		queue = mock(Queue.class);
		request1 = givenRequest(42);
		givenPollStrategy(request1);

		given(queue.poll()).willReturn(null);
		given(queue.size()).willReturn(0);

		given(channel.send(any(), any())).willThrow(new IOException());

		given(batchPolicy.calculateTimeWindow(1)).willReturn(30L);
		given(timeMachine.nanoNow()).willReturn(10L,  45L, 60L, 80L, 99L);
		given(timeMachine.nanoElapsed(10L)).willReturn(10L, 45L);
		givenBatchSendStrategy(batchPolicy, pollStrategy);

		// WHEN
		strategy.send(queue);

		// THEN
		verify(metrics).incrementErrorCount();

		verify(metrics, never()).recordPacketSent(anyLong());
		verify(metrics, never()).addSentBytes(anyLong());
		verify(metrics, never()).incrementPartiallySentPacketCount();
		verify(metrics, never()).incrementUnsentPacketCount();
		verify(metrics, never()).addDroppedBytes(anyLong());

		verify(request1).release();
	}

	private static Stream<Arguments> send_multiple() {
		return Stream.of(
				Arguments.of(Named.of("one", requestList(142)),
						30L,
						List.of(10L, 45L),
						List.of(
								List.of(10L, 10L, 45L),
								List.of(45L, 100L)
						),
						2, 1, 1
				),
				Arguments.of(Named.of("half-batch", requestList(142, 39, 69)),
						30L,
						List.of(10L, 45L, 60L, 80L, 99L),
						List.of(
								List.of(10L, 10L, 20L, 29L, 30L),
								List.of(45L, 11L),
								List.of(60L, 12L),
								List.of(80L, 13L)
						),
						4, 3, 3
				),
				Arguments.of(Named.of("full-batch", requestList(142, 39, 69, 51, 48, 64)),
						30L,
						List.of(10L, 45L, 46L, 48L, 51L, 55L, 60L, 99L),
						List.of(
								List.of(10L, 11L, 12L, 13L, 14L, 15L, 16L, 45L),
								List.of(45L, 21L),
								List.of(46L, 22L),
								List.of(48L, 23L),
								List.of(51L, 24L),
								List.of(55L, 25L),
								List.of(60L, 30L)
						),
						7, 5, 6
				),
				Arguments.of(Named.of("more", requestList(142, 39, 69, 51, 48, 64, 66)),
						30L,
						List.of(10L, 45L, 46L, 48L, 51L, 55L, 60L, 99L),
						List.of(
								List.of(10L, 11L, 12L, 13L, 14L, 15L, 16L, 45L),
								List.of(45L, 21L),
								List.of(46L, 22L),
								List.of(48L, 23L),
								List.of(51L, 24L),
								List.of(55L, 25L),
								List.of(60L, 30L)
						),
						7, 5, 6
				)
		);
	}

	@ParameterizedTest
	@MethodSource("send_multiple")
	void send_multiple(
			List<SendRequest> availableRequests,
			long timeWindow,
			List<Long> nanoNow,
			List<List<Long>> nanoElapsed,
			int expectedNanoNowCall,
			int expectedPollCount,
			int expectedSentPacketCount
	) throws IOException {
		// GIVEN
		int requestCount = availableRequests.size();

		givenBatchPolicy(BATCH_SIZE);
		Queue<SendRequest> queue = mock(Queue.class);

		// First request is always handled by pollStrategy
		SendRequest first = availableRequests.getFirst();
		givenPollStrategy(first);

		// Pre-evaluate all request arguments for channel.send()
		List<ByteBuffer> bufs = new ArrayList<>();
		List<SocketAddress> addrs = new ArrayList<>();
		List<Integer> sizes = new ArrayList<>();

		for (int i = 0; i < requestCount; i++) {
			SendRequest req = availableRequests.get(i);
			bufs.add(req.getBuffer());
			addrs.add(req.getTarget());
			sizes.add(req.writeableSize());
		}

		// queue.poll() returns all requests except the first
		SendRequest[] remaining = availableRequests.subList(1, availableRequests.size())
				.toArray(new SendRequest[0]);

		if (remaining.length == 0) {
			given(queue.poll()).willReturn(null);
		} else if (remaining.length == 1) {
			given(queue.poll()).willReturn(remaining[0], (SendRequest) null);
		} else {
			SendRequest[] tail = new SendRequest[remaining.length];
			System.arraycopy(remaining,1, tail, 0, remaining.length - 1);
			tail[remaining.length - 1] = null;
			given(queue.poll()).willReturn(remaining[0], tail);
		}

		// queue.size()
		given(queue.size()).willReturn(remaining.length);

		// batchPolicy
		given(batchPolicy.calculateTimeWindow(requestCount - 1))
				.willReturn(timeWindow);

		// channel.send() stubbing using pre-evaluated arguments
		for (int i = 0; i < requestCount; i++) {
			given(channel.send(bufs.get(i), addrs.get(i)))
					.willReturn(sizes.get(i));
		}

		// timeMachine.nanoNow()
		Long[] nowArray = nanoNow.toArray(new Long[0]);
		if (nowArray.length == 1) {
			given(timeMachine.nanoNow()).willReturn(nowArray[0]);
		} else {
			Long[] tail = Arrays.copyOfRange(nowArray, 1, nowArray.length);
			given(timeMachine.nanoNow()).willReturn(nowArray[0], tail);
		}

		// timeMachine.nanoElapsed()
		for (List<Long> elapse : nanoElapsed) {
			long start = elapse.getFirst();
			Long[] returns = elapse.subList(1, elapse.size()).toArray(new Long[0]);

			if (returns.length == 1) {
				given(timeMachine.nanoElapsed(start)).willReturn(returns[0]);
			} else {
				Long[] tail = Arrays.copyOfRange(returns, 1, returns.length);
				given(timeMachine.nanoElapsed(start)).willReturn(returns[0], tail);
			}
		}

		givenBatchSendStrategy(batchPolicy, pollStrategy);

		// WHEN
		int actual = strategy.send(queue);

		// THEN
		assertThat(actual).isEqualTo(expectedSentPacketCount);

		verify(timeMachine, times(expectedNanoNowCall)).nanoNow();
		verify(pollStrategy).poll(queue);
		if (expectedPollCount == 0) {
			verify(queue, never()).poll();
		}
		else if (expectedPollCount > 0) {
			verify(queue, times(expectedPollCount)).poll();
		}

		verify(metrics, times(expectedSentPacketCount)).recordPacketSent(sentBytesCaptor.capture());
		assertThat(sentBytesCaptor.getAllValues()).hasSize(expectedSentPacketCount);
		for (int i = 0; i < expectedSentPacketCount; i++) {
			assertThat(sentBytesCaptor.getAllValues().get(i))
					.isEqualTo(nanoElapsed.get(i + 1).get(1));
		}

		verify(metrics, times(expectedSentPacketCount)).addSentBytes(metricsBytesCaptor.capture());
		assertThat(metricsBytesCaptor.getAllValues()).hasSize(expectedSentPacketCount);
		for (int i = 0; i < expectedSentPacketCount; i++) {
			assertThat(metricsBytesCaptor.getAllValues().get(i))
					.isEqualTo(availableRequests.get(i).getBuffer().capacity());
		}

		verify(metrics, never()).incrementPartiallySentPacketCount();
		verify(metrics, never()).addDroppedBytes(anyLong());
	}

	private static List<SendRequest> requestList(@NonNull int... sizes) {
		List<SendRequest> requests = new ArrayList<>();
		for (int size : sizes) {
			requests.add(givenRequest(size));
		}
		return requests;
	}

	private static SendRequest givenRequest(int size) {
		return givenRequest(size, true);
	}

	private static SendRequest givenRequest(int size, boolean release) {
		SendRequest request = mock(SendRequest.class);
		ByteBuffer buffer = ByteBuffer.allocate(size);
		given(request.getBuffer()).willReturn(buffer);
		given(request.writeableSize()).willReturn(size);
		given(request.getTarget()).willReturn(new InetSocketAddress("localhost", 5555));
		given(request.isRelease()).willReturn(release);
		return request;
	}

	private void givenBatchPolicy(int batchSize) {
		batchPolicy = mock(BatchPolicy.class);
		given(batchPolicy.batchSize()).willReturn(batchSize);
	}

	private void givenPollStrategy(SendRequest... requests) {
		pollStrategy = mock(PollStrategy.class);
		if (requests != null) {
			if (requests.length == 1) {
				given(pollStrategy.poll(any())).willReturn(requests[0], (SendRequest) null);
			} else {
				given(pollStrategy.poll(any())).willReturn(requests[0], (SendRequest[]) Arrays.copyOfRange(requests, 1, requests.length));
			}
		}
	}

	private void givenBatchSendStrategy(BatchPolicy batchPolicy, PollStrategy pollStrategy) {
		strategy = new BatchSendStrategy(batchPolicy, pollStrategy, channel, timeMachine, metrics, bufferPool);
	}

	private BatchSendStrategy givenStrategy() {
		return new BatchSendStrategy(batchPolicy, pollStrategy, channel, timeMachine, metrics, bufferPool);
	}

	private void verifyRequestReleased(SendRequest request, boolean release) {
		verify(request).release();
		if (release) {
			verify(bufferPool).release(request.getBuffer());
		}
		else {
			verify(bufferPool, never()).release(request.getBuffer());
		}
	}

	private void givenTwoRequestsInTheQueue(
			int packet1size,
			int packet2size,
			int sentBytes1,
			int sentBytes2
	) throws IOException {
		givenTwoRequestsInTheQueue(packet1size, packet2size, sentBytes1, sentBytes2, true, true);
	}

	private void givenTwoRequestsInTheQueue(
			int packet1size,
			int packet2size,
			int sentBytes1,
			int sentBytes2,
			boolean freePacket1,
			boolean freePacket2
	) throws IOException {
		givenBatchPolicy(BATCH_SIZE);
		queue = mock(Queue.class);
		request1 = givenRequest(packet1size, freePacket1);
		request2 = givenRequest(packet2size, freePacket2);
		givenPollStrategy(request1);

		given(queue.poll()).willReturn(request2, (SendRequest) null);
		given(queue.size()).willReturn(1);

		given(channel.send(request1.getBuffer(), request1.getTarget())).willReturn(sentBytes1);
		given(channel.send(request2.getBuffer(), request2.getTarget())).willReturn(sentBytes2);

		given(batchPolicy.calculateTimeWindow(1)).willReturn(30L);
		given(timeMachine.nanoNow()).willReturn(10L, 45L, 60L, 80L, 99L);
		given(timeMachine.nanoElapsed(10L)).willReturn(10L, 45L);
		given(timeMachine.nanoElapsed(45L)).willReturn(11L);

		givenBatchSendStrategy(batchPolicy, pollStrategy);
	}

}