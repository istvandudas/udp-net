package org.net.endpoint.common;

import javax.annotation.processing.Generated;

/**
 * <a href="https://github.com/aappleby/smhasher/wiki/MurmurHash3">Source of the algorithm.</a> *
 */
public final class Murmur3 {

	private static final long C1 = 0x87c37b91114253d5L;
	private static final long C2 = 0x4cf5ad432745937fL;

	@Generated("ignore")
	private Murmur3() {}

	public static long hash64(byte[] data) {
		long h1 = 0;
		long h2 = 0;

		int length = data.length;
		int roundedEnd = (length & 0xFFFFFFF0);

		// ---- body ----
		for (int i = 0; i < roundedEnd; i += 16) {
			long k1 = getLongLE(data, i);
			long k2 = getLongLE(data, i + 8);

			k1 *= C1;
			k1 = Long.rotateLeft(k1, 31);
			k1 *= C2;
			h1 ^= k1;

			h1 = Long.rotateLeft(h1, 27);
			h1 += h2;
			h1 = h1 * 5 + 0x52dce729;

			k2 *= C2;
			k2 = Long.rotateLeft(k2, 33);
			k2 *= C1;
			h2 ^= k2;

			h2 = Long.rotateLeft(h2, 31);
			h2 += h1;
			h2 = h2 * 5 + 0x38495ab5;
		}

		// ---- tail ----
		long k1 = 0;
		long k2 = 0;

		int tail = length & 15;

		switch (tail) {
			case 15: k2 ^= ((long) data[roundedEnd + 14] & 0xff) << 48;
			case 14: k2 ^= ((long) data[roundedEnd + 13] & 0xff) << 40;
			case 13: k2 ^= ((long) data[roundedEnd + 12] & 0xff) << 32;
			case 12: k2 ^= ((long) data[roundedEnd + 11] & 0xff) << 24;
			case 11: k2 ^= ((long) data[roundedEnd + 10] & 0xff) << 16;
			case 10: k2 ^= ((long) data[roundedEnd + 9] & 0xff) << 8;
			case 9:  k2 ^= ((long) data[roundedEnd + 8] & 0xff);
				k2 *= C2;
				k2 = Long.rotateLeft(k2, 33);
				k2 *= C1;
				h2 ^= k2;

			case 8:  k1 ^= ((long) data[roundedEnd + 7] & 0xff) << 56;
			case 7:  k1 ^= ((long) data[roundedEnd + 6] & 0xff) << 48;
			case 6:  k1 ^= ((long) data[roundedEnd + 5] & 0xff) << 40;
			case 5:  k1 ^= ((long) data[roundedEnd + 4] & 0xff) << 32;
			case 4:  k1 ^= ((long) data[roundedEnd + 3] & 0xff) << 24;
			case 3:  k1 ^= ((long) data[roundedEnd + 2] & 0xff) << 16;
			case 2:  k1 ^= ((long) data[roundedEnd + 1] & 0xff) << 8;
			case 1:  k1 ^= ((long) data[roundedEnd] & 0xff);
				k1 *= C1;
				k1 = Long.rotateLeft(k1, 31);
				k1 *= C2;
				h1 ^= k1;
		}

		// ---- finalization ----
		h1 ^= length;
		h2 ^= length;

		h1 += h2;
		h2 += h1;

		h1 = fmix64(h1);
		h2 = fmix64(h2);

		h1 += h2;

		return h1;
	}

	public static long hash128ToLong(byte[] a, byte[] b) {
		long h1 = 0;
		long h2 = 0;

		// ---- block 1 ----
		long k1 = getLongLE(a, 0);
		long k2 = getLongLE(a, 8);

		k1 *= C1;
		k1 = Long.rotateLeft(k1, 31);
		k1 *= C2;
		h1 ^= k1;

		h1 = Long.rotateLeft(h1, 27);
		h1 += h2;
		h1 = h1 * 5 + 0x52dce729;

		k2 *= C2;
		k2 = Long.rotateLeft(k2, 33);
		k2 *= C1;
		h2 ^= k2;

		h2 = Long.rotateLeft(h2, 31);
		h2 += h1;
		h2 = h2 * 5 + 0x38495ab5;

		// ---- block 2 ----
		k1 = getLongLE(b, 0);
		k2 = getLongLE(b, 8);

		k1 *= C1;
		k1 = Long.rotateLeft(k1, 31);
		k1 *= C2;
		h1 ^= k1;

		h1 = Long.rotateLeft(h1, 27);
		h1 += h2;
		h1 = h1 * 5 + 0x52dce729;

		k2 *= C2;
		k2 = Long.rotateLeft(k2, 33);
		k2 *= C1;
		h2 ^= k2;

		h2 = Long.rotateLeft(h2, 31);
		h2 += h1;
		h2 = h2 * 5 + 0x38495ab5;

		// ---- finalization ----
		h1 ^= 32; // total length = 32 bytes
		h2 ^= 32;

		h1 += h2;
		h2 += h1;

		h1 = fmix64(h1);
		h2 = fmix64(h2);

		h1 += h2;

		return h1;
	}

	private static long getLongLE(byte[] b, int i) {
		return ((long) b[i] & 0xff) |
				(((long) b[i + 1] & 0xff) << 8) |
				(((long) b[i + 2] & 0xff) << 16) |
				(((long) b[i + 3] & 0xff) << 24) |
				(((long) b[i + 4] & 0xff) << 32) |
				(((long) b[i + 5] & 0xff) << 40) |
				(((long) b[i + 6] & 0xff) << 48) |
				(((long) b[i + 7] & 0xff) << 56);
	}

	private static long fmix64(long k) {
		k ^= k >>> 33;
		k *= 0xff51afd7ed558ccdL;
		k ^= k >>> 33;
		k *= 0xc4ceb9fe1a85ec53L;
		k ^= k >>> 33;
		return k;
	}
}
