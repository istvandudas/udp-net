package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.TestUtil;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class UnreliableUdpEndpointExceptionTest {

	// the test subject
	private TestUnreliableUdpEndpoint endpoint;

	private TimeMachine timeMachine;
	private BufferPool bufferPool;

	@BeforeEach
	void setUp() {
		timeMachine = mock(TimeMachine.class);
		bufferPool = mock(BufferPool.class);
	}

	@Test
	void closeChannelException() throws IOException {
		// GIVEN
		endpoint = new TestUnreliableUdpEndpoint(TestUtil.epConfig("test", 0), timeMachine, bufferPool);
		endpoint.setRunning(true);


		ByteBuffer incomingBuffer = ByteBuffer.allocate(1024);
		endpoint.setIncomingBuffer(incomingBuffer);
		UdpDatagramChannel channel = mock(UdpDatagramChannel.class);
		endpoint.setChannel(channel);
		given(channel.receive(incomingBuffer)).willThrow(new ClosedChannelException());

		// WHEN
		assertDoesNotThrow(() -> endpoint.run());
	}

	@Test
	void outerException() {
		// GIVEN
		endpoint = new TestUnreliableUdpEndpoint(TestUtil.epConfig("test", 0), timeMachine, bufferPool);
		endpoint.setRunning(true);

		// WHEN
		assertDoesNotThrow(() -> endpoint.run());
	}

}