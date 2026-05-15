package org.net.endpoint.common;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.within;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TimeMachineTest {

	@Test
	void testNowReturnsSystemMillis() {
		// GIVEN
		TimeMachine tm = new TimeMachine();

		// WHEN
		long result = tm.now();

		// THEN
		assertThat(result).isCloseTo(System.currentTimeMillis(), within(50L));
	}

	@Test
	void testNanoNowReturnsIncreasingValues() {
		// GIVEN
		TimeMachine tm = new TimeMachine();

		// WHEN
		long first = tm.nanoNow();
		long second = tm.nanoNow();

		// THEN
		assertThat(second).isGreaterThanOrEqualTo(first);
	}


	@Test
	void testNanoElapsed() {
		// GIVEN
		TimeMachine tm = spy(new TimeMachine());
		when(tm.nanoNow()).thenReturn(200L);

		// WHEN
		long elapsed = tm.nanoElapsed(100L);

		// THEN
		assertThat(elapsed).isEqualTo(100L);
	}

	@Test
	void testTimeoutTrue() {
		// GIVEN
		TimeMachine tm = spy(new TimeMachine());
		when(tm.nanoNow()).thenReturn(500L);

		// WHEN
		boolean result = tm.timeout(100L, 300L);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	void testTimeoutFalse() {
		// GIVEN
		TimeMachine tm = spy(new TimeMachine());
		when(tm.nanoNow()).thenReturn(150L);

		// WHEN
		boolean result = tm.timeout(500L, 300L);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	void testSleepRemainingNanosSleepsWhenRemainingPositive() {
		// GIVEN
		TimeMachine tm = spy(new TimeMachine());
		try (var mocked = Mockito.mockStatic(LockSupport.class)) {
			when(tm.nanoNow()).thenReturn(150L);

			// WHEN
			tm.sleepRemainingNanos(300L, 100L);

			// THEN
			mocked.verify(() -> LockSupport.parkNanos(250L));
		}
	}

	@Test
	void testSleepRemainingNanosDoesNotSleepWhenRemainingZeroOrNegative() {
		// GIVEN
		TimeMachine tm = spy(new TimeMachine());
		try (var mocked = Mockito.mockStatic(LockSupport.class)) {
			when(tm.nanoNow()).thenReturn(500L);

			// WHEN
			tm.sleepRemainingNanos(300L, 100L);

			// THEN
			mocked.verifyNoInteractions();
		}
	}
}
