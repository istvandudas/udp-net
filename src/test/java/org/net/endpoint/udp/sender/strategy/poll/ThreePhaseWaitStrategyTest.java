package org.net.endpoint.udp.sender.strategy.poll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"rawtypes", "unchecked", "DataFlowIssue"})
class ThreePhaseWaitStrategyTest {

	private static final int SPIN_COUNT = 2;
	private static final long PARK_NANOS = 100_000L;

	// the test subject
	private ThreePhaseWaitStrategy<String> strategy;

	@BeforeEach
	void setUp() {
		strategy = new ThreePhaseWaitStrategy<>(SPIN_COUNT, PARK_NANOS);
	}

	@Test
	void poll() {
		// GIVEN
		Queue<String> queue = mock(Queue.class);
		String item = "test";
		given(queue.poll()).willReturn(item);

		// WHEN
		String actual = strategy.poll(queue);

		// THEN
		assertThat(actual).isSameAs(item);
		verify(queue).poll();
	}

	@Test
	void poll_EmptyQueue() {
		// GIVEN
		Queue<String> queue = mock(Queue.class);
		given(queue.poll()).willReturn(null);

		// WHEN
		String actual = strategy.poll(queue);

		// THEN
		assertThat(actual).isNull();
		verify(queue, times(4)).poll();
	}

	@Test
	void poll_findInBusySpinWait() {
		// GIVEN
		Queue<String> queue = mock(Queue.class);
		String item = "test";
		given(queue.poll()).willReturn(null, item);

		// WHEN
		String actual = strategy.poll(queue);

		// THEN
		assertThat(actual).isSameAs(item);
		verify(queue, times(2)).poll();
	}

	@Test
	void poll_findAtFallbackPhase() {
		// GIVEN
		Queue<String> queue = mock(Queue.class);
		String item = "test";
		given(queue.poll()).willReturn(null, null, item);

		// WHEN
		String actual = strategy.poll(queue);

		// THEN
		assertThat(actual).isSameAs(item);
		verify(queue, times(3)).poll();
	}

	@Test
	void poll_findAtParkPhase() {
		// GIVEN
		Queue<String> queue = mock(Queue.class);
		String item = "test";
		given(queue.poll()).willReturn(null, null, null, item);

		// WHEN
		String actual = strategy.poll(queue);

		// THEN
		assertThat(actual).isSameAs(item);
		verify(queue, times(4)).poll();
	}

	@Test
	void poll_queue_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> strategy.poll(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("queue is marked non-null but is null");
	}
}