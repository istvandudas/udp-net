package org.net.endpoint.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TokenBucketTest {

	private static final long ONE_SEC = Duration.ofSeconds(1).toNanos();
	private static final long FIVE_SEC = Duration.ofSeconds(5).toNanos();
	private static final long TEN_SEC = Duration.ofSeconds(10).toNanos();

	@Mock
	private TimeMachine timeMachine;

	@Test
	void constructFails_timeMachine_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TokenBucket(3, 1, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("timeMachine is marked non-null but is null");
	}

	@Test
	void consumesUntilEmpty() {
		// GIVEN
		given(timeMachine.nanoNow()).willReturn(0L);
		TokenBucket bucket = new TokenBucket(3, 1, timeMachine);

		// WHEN
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isTrue();

		// THEN empty
		assertThat(bucket.tryConsume()).isFalse();
	}

	@Test
	void refillsOverTime() {
		// GIVEN
		given(timeMachine.nanoNow()).willReturn(0L, 0L, 0L, 0L, ONE_SEC, ONE_SEC);
		TokenBucket bucket = new TokenBucket(2, 1, timeMachine);

		// WHEN
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isFalse();

		// THEN 1 refilled
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isFalse();
	}

	@Test
	void doesNotExceedCapacity() {
		// GIVEN
		given(timeMachine.nanoNow()).willReturn(0L, 0L, 0L, 0L, 0L, 0L, 0L, ONE_SEC, ONE_SEC, ONE_SEC, ONE_SEC, ONE_SEC, ONE_SEC);
		TokenBucket bucket = new TokenBucket(5, 10, timeMachine);
		for (int i = 0; i < 5; i++) {
			assertThat(bucket.tryConsume()).isTrue();
		}
		assertThat(bucket.tryConsume()).isFalse();

		// WHEN
		for (int i = 0; i < 5; i++) {
			assertThat(bucket.tryConsume()).isTrue();
		}

		// THEN
		assertThat(bucket.tryConsume()).isFalse();
	}

	@Test
	void refillRateCanBeAdjusted() {
		// GIVEN
		given(timeMachine.nanoNow()).willReturn(0L, 0L, 0L, 0L, FIVE_SEC, FIVE_SEC, FIVE_SEC);
		TokenBucket bucket = new TokenBucket(2, 1, timeMachine);
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isFalse();

		// WHEN
		bucket.refillPerSecond(2);

		// THEN
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isFalse();
	}

	@Test
	void noRefillWhenTimeDoesNotAdvance() {
		// GIVEN
		given(timeMachine.nanoNow()).willReturn(0L);
		TokenBucket bucket = new TokenBucket(1, 1, timeMachine);

		// WHEN
		assertThat(bucket.tryConsume()).isTrue();
		assertThat(bucket.tryConsume()).isFalse();

		// THEN no time advanced, still empty
		assertThat(bucket.tryConsume()).isFalse();
	}
}