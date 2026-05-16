package org.net.endpoint.udp.sender;

import org.junit.jupiter.api.Test;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.endpoint.UdpEndpoint;
import org.net.endpoint.udp.sender.strategy.send.BatchSendStrategy;
import org.net.endpoint.udp.sender.strategy.send.SendStrategy;

import org.net.endpoint.udp.endpoint.UdpDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DataFlowIssue", "unchecked", "FieldCanBeLocal", "rawtypes"})
class PacketSenderTest {

	private static final InetSocketAddress ADDRESS = new InetSocketAddress("localhost", 5432);

	// the test subject
	private PacketSender sender;
	private TimeMachine timeMachine;
	private UdpDatagramChannel channel;
	private Queue queue;
	private PacketSenderMetrics metrics;
	private BufferPool bufferPool;
	private ByteBuffer buffer;
	private Supplier<SendRequest> sendRequestSupplier;

	@Test
	void construct() {
		// GIVEN + WHEN
		PacketSender actual = new PacketSender(
				"test",
				mock(PacketSenderMetrics.class),
				mock(SendStrategy.class)
		);

		// THEN
		assertThat(actual).isNotNull();
	}

	@Test
	void construct_name_IsNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSender(
				null,
				mock(PacketSenderMetrics.class),
				mock(SendStrategy.class),
				mock(Queue.class),
				mock(Supplier.class)
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("name is marked non-null but is null");
	}

	@Test
	void construct_metrics_IsNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSender(
				"test",
				null,
				mock(SendStrategy.class),
				mock(Queue.class),
				mock(Supplier.class)
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("metrics is marked non-null but is null");
	}

	@Test
	void construct_sendStrategy_IsNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSender(
				"test",
				mock(PacketSenderMetrics.class),
				null,
				mock(Queue.class),
				mock(Supplier.class)
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("sendStrategy is marked non-null but is null");
	}

	@Test
	void metricsIsAView() {
		// GIVEN
		givenSender();

		// WHEN
		Object actual = sender.metrics();

		// THEN
		assertThat(actual).isInstanceOf(PacketSenderMetricsView.class);
	}

	@Test
	void send() {
		// GIVEN
		queue = mock(Queue.class);
		given(queue.offer(any())).willReturn(true);
		givenSender(queue);
		buffer = givenBuffer();
		given(sendRequestSupplier.get()).willReturn(SendRequest.create());

		// WHEN
		boolean actual = sender.send(buffer, ADDRESS, true);

		// THEN
		assertThat(actual).isTrue();
		assertThat(sender.metrics().enqueuedPacketCount()).isOne();
		verify(queue).offer(any(SendRequest.class));
	}

	@Test
	void send_cannotRegisterMoreSendRequest() {
		// GIVEN
		queue = mock(Queue.class);
		given(queue.offer(any())).willReturn(true);
		givenSender(queue);
		buffer = givenBuffer();

		// WHEN
		boolean actual = sender.send(buffer, ADDRESS, true);

		// THEN
		assertThat(actual).isFalse();
		assertThat(sender.metrics().enqueuedPacketCount()).isZero();
		verify(queue, never()).offer(any(SendRequest.class));
	}


	@Test
	void send_queueOfferFails() {
		// GIVEN
		queue = mock(Queue.class);
		given(queue.offer(any())).willReturn(false);
		givenSender(queue);
		given(sendRequestSupplier.get()).willReturn(SendRequest.create());
		buffer = givenBuffer();

		// WHEN
		boolean actual = sender.send(buffer, ADDRESS, true);

		// THEN
		assertThat(actual).isFalse();
		assertThat(sender.metrics().enqueuedPacketCount()).isZero();
	}

	@Test
	void send_ByteBuffer_IsNull() {
		// GIVEN
		givenSender();

		// WHEN + THEN
		assertThatThrownBy(() -> sender.send(null, ADDRESS, true))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("buffer is marked non-null but is null");
	}

	@Test
	void send_address_IsNull() {
		// GIVEN
		givenSender();


		// WHEN + THEN
		assertThatThrownBy(() -> sender.send(givenBuffer(), null, true))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("target is marked non-null but is null");
	}

	private void givenSender() {
		givenSender(null);
	}

	private void givenSender(Queue<SendRequest> queue) {
		channel = mock(UdpDatagramChannel.class);
		timeMachine = mock(TimeMachine.class);
		metrics = new PacketSenderMetrics();
		bufferPool = mock(BufferPool.class);
		sendRequestSupplier = mock(Supplier.class);

		sender = new PacketSender(
				"test-sender",
				metrics,
				new BatchSendStrategy(channel, timeMachine, metrics, bufferPool),
				queue,
				sendRequestSupplier
		);
	}

	private ByteBuffer givenBuffer() {
		return ByteBuffer.allocateDirect(UdpEndpoint.SAFE_MTU_SIZE);
	}

}