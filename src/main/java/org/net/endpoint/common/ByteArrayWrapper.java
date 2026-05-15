package org.net.endpoint.common;


import java.util.Arrays;

public class ByteArrayWrapper {
	private final byte[] data;
	private final int hash;

	ByteArrayWrapper(byte[] data) {
		this.data = Arrays.copyOf(data, data.length);
		this.hash = Arrays.hashCode(this.data);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ByteArrayWrapper other)) return false;
		return Arrays.equals(this.data, other.data);
	}

	@Override
	public int hashCode() {
		return hash;
	}
}