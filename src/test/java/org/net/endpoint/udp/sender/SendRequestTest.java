package org.net.endpoint.udp.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.common.ObjectPoolStat;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SendRequestTest {

	// the test subject
	private SendRequest request;

	private ByteBuffer buffer;
	private SocketAddress address;

	@BeforeEach
	void setUp() {
		buffer = ByteBuffer.allocateDirect(1024);
		address = mock(SocketAddress.class);
	}

	@Test
	void set() {
		// GIVEN
		SendRequest request = SendRequest.create();

		// WHEN
		request.set(buffer, address, true);

		// THEN
		assertThat(request.buffer()).isEqualTo(buffer);
		assertThat(request.target()).isEqualTo(address);
		assertThat(request.releaseAfterSend()).isTrue();
	}

	@Test
	void writeableSize() {
		// GIVEN
		SendRequest request = SendRequest.create();
		request.set(buffer, address, true);
		buffer.position(42).flip();

		// WHEN
		int actual = request.writeableSize();

		// THEN
		assertThat(actual).isEqualTo(buffer.remaining());
	}

	@Test
	void writeableSize_buffer_IsNull() {
		// GIVEN
		SendRequest request = SendRequest.create();

		// WHEN
		int actual = request.writeableSize();

		// THEN
		assertThat(actual).isZero();
	}

	@Test
	void clear() {
		// GIVEN
		SendRequest request = SendRequest.create();
		request.set(buffer, address, true);
		buffer.position(42).flip();

		// WHEN
		request.clear();

		// THEN
		assertThat(request.buffer()).isNull();
		assertThat(request.target()).isNull();
		assertThat(request.releaseAfterSend()).isFalse();
	}

	@Test
	void stat() {
		// GIVEN
		SendRequest request = SendRequest.create();

		// WHEN
		String actual = SendRequest.stat();

		// THEN
		assertThat(actual).startsWith("SendRequest[");
		assertThat(actual).endsWith("]");
	}

	@Test
	void poolStat() {
		// GIVEN
		SendRequest request = SendRequest.create();

		// WHEN
		Object actual = SendRequest.poolStat();

		// THEN
		assertThat(actual).isInstanceOf(ObjectPoolStat.class);
	}

}