package org.net.endpoint.udp.sender.strategy.send;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueueDepthMicroBatchPolicyTest {

	// the test subject
	private QueueDepthMicroBatchPolicy policy;

	@BeforeEach
	void setUp() {
		policy = new QueueDepthMicroBatchPolicy();
	}

	@Test
	void batchSize() {
		// GIVEN + WHEN + THEN
		assertThat(policy.batchSize()).isEqualTo(6);
	}


	private static Stream<Arguments> calculateTimeWindow() {
		return Stream.of(
			Arguments.of(0, 20_000L),
				Arguments.of(0, 20_000L),
				Arguments.of(1, 20_000L),
				Arguments.of(2, 20_000L),
				Arguments.of(3, 35_000L),
				Arguments.of(4, 35_000L),
				Arguments.of(5, 50_000L),
				Arguments.of(6, 50_000L),
				Arguments.of(7, 50_000L),
				Arguments.of(8, 50_000L),
				Arguments.of(9, 75_000L),
				Arguments.of(10, 75_000L),
				Arguments.of(11, 75_000L),
				Arguments.of(12, 75_000L),
				Arguments.of(13, 75_000L),
				Arguments.of(14, 75_000L),
				Arguments.of(15, 75_000L),
				Arguments.of(16, 75_000L),
				Arguments.of(17, 100_000L),
				Arguments.of(1000, 100_000L)
		);
	}

	@ParameterizedTest
	@MethodSource("calculateTimeWindow")
	void calculateTimeWindow(int depth, long expected) {
		// GIVEN + WHEN
		long actual = policy.calculateTimeWindow(depth);

		// THEN
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void calculateTimeWindow_invalid_queueSize() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> policy.calculateTimeWindow(-1))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("queueSize can't be negative!");
	}
}