package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class Murmur3Test {

	@Test
	void emptyArray() {
		// GIVEN
		byte[] empty = new byte[0];

		// WHEN
		long actual = Murmur3.hash64(empty);

		// THEN
		assertThat(actual).isEqualTo(0x0000000000000000L);
	}

	@Test
	void singleByte() {
		// GIVEN
		byte[] data = { 0x01 };

		// WHEN
		long actual = Murmur3.hash64(data);

		// THEN
		assertThat(actual).isEqualTo(8849112093580131862L);
	}

	@Test
	void shortString() {
		// GIVEN + WHEN
		long actual = hash64("abc");

		// THEN
		assertThat(actual).isEqualTo(-5434086359492102041L);
	}

	@Test
	void longerString() {
		// GIVEN + WHEN
		long actual = hash64("The quick brown fox jumps over the lazy dog");

		// THEN
		assertThat(actual).isEqualTo(-2068352364225029268L);
	}

	@Test
	void aligned16Bytes() {
		// GIVEN
		byte[] data = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

		// WHEN
		long actual = Murmur3.hash64(data);

		// THEN
		assertThat(actual).isEqualTo(9143696100365753614L);
	}

	@Test
	void unalignedTail() {
		// GIVEN
		byte[] data = "0123456789ABCDE".getBytes(StandardCharsets.UTF_8);

		// WHEN
		long actual = Murmur3.hash64(data);

		// THEN
		assertThat(actual).isEqualTo(-3452538057812668377L);
	}

	@Test
	void randomStability() {
		// GIVEN
		Random rnd = new Random(12345);

		for (int i = 0; i < 100; i++) {
			byte[] data = new byte[rnd.nextInt(64)];
			rnd.nextBytes(data);

			// WHEN
			long h1 = Murmur3.hash64(data);
			long h2 = Murmur3.hash64(data);

			assertThat(h1).isEqualTo(h2);
		}
	}

	@Test
	void hash128ToLong() {
		// GIVEN
		byte[] a = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
		byte[] b = "FEDCBA9876543210".getBytes(StandardCharsets.UTF_8);

		// WHEN
		long actual = Murmur3.hash128ToLong(a, b);

		// THEN
		assertThat(actual).isEqualTo(-2494950571653251617L);
	}

	@Test
	void hash128Deterministic() {
		// GIVEN
		byte[] a = new byte[16];
		byte[] b = new byte[16];

		new Random(999).nextBytes(a);
		new Random(999).nextBytes(b);

		// WHEN
		long h1 = Murmur3.hash128ToLong(a, b);
		long h2 = Murmur3.hash128ToLong(a, b);

		// THEN
		assertThat(h1).isEqualTo(h2);
	}

	@Test
	void getLongLE() {
		// GIVEN
		byte[] b = {
				(byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
				(byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08
		};

		// WHEN
		long actual = invokeGetLongLE(b, 0);

		// THEN
		assertThat(actual).isEqualTo(0x0807060504030201L);
	}


	private static long invokeGetLongLE(byte[] b, int i) {
		try {
			var m = Murmur3.class.getDeclaredMethod("getLongLE", byte[].class, int.class);
			m.setAccessible(true);
			return (long) m.invoke(null, b, i);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private long hash64(String s) {
		return Murmur3.hash64(s.getBytes(StandardCharsets.UTF_8));
	}

}