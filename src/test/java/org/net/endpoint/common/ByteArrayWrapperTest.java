package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ByteArrayWrapperTest {

	@Test
	void testEqualsSameInstance() {
		// GIVEN
		ByteArrayWrapper w = new ByteArrayWrapper(new byte[]{1, 2, 3});

		// WHEN
		boolean eq = w.equals(w);

		// THEN
		assertThat(eq).isTrue();
	}

	@Test
	void testEqualsSameContent() {
		// GIVEN
		ByteArrayWrapper w1 = new ByteArrayWrapper(new byte[]{1, 2, 3});
		ByteArrayWrapper w2 = new ByteArrayWrapper(new byte[]{1, 2, 3});

		// WHEN
		boolean eq = w1.equals(w2);

		// THEN
		assertThat(eq).isTrue();
		assertThat(w1.hashCode()).isEqualTo(w2.hashCode());
	}

	@Test
	void testNotEqualsDifferentContent() {
		// GIVEN
		ByteArrayWrapper w1 = new ByteArrayWrapper(new byte[]{1, 2, 3});
		ByteArrayWrapper w2 = new ByteArrayWrapper(new byte[]{1, 2, 4});

		// WHEN
		boolean eq = w1.equals(w2);

		// THEN
		assertThat(eq).isFalse();
	}

	@Test
	void testNotEqualsDifferentType() {
		// GIVEN
		ByteArrayWrapper w = new ByteArrayWrapper(new byte[]{1});
		Object other = mock(Object.class);

		// WHEN
		boolean eq = w.equals(other);

		// THEN
		assertThat(eq).isFalse();
	}

	@Test
	void testHashCodeConsistent() {
		// GIVEN
		ByteArrayWrapper w = new ByteArrayWrapper(new byte[]{9, 9, 9});

		// WHEN
		int h1 = w.hashCode();
		int h2 = w.hashCode();

		// THEN
		assertThat(h1).isEqualTo(h2);
	}

	@Test
	void testDefensiveCopy() {
		// GIVEN
		byte[] arr = {5, 6, 7};
		ByteArrayWrapper w = new ByteArrayWrapper(arr);

		// WHEN
		arr[0] = 99;
		ByteArrayWrapper w2 = new ByteArrayWrapper(new byte[]{5, 6, 7});

		// THEN
		assertThat(w).isEqualTo(w2);
	}
}
