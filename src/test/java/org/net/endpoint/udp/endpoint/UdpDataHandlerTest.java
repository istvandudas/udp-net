package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.net.endpoint.udp.connection.UdpConnection;

import java.nio.ByteBuffer;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.net.endpoint.TestUtil.CSI;
import static org.net.endpoint.TestUtil.SSI;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpDataHandlerTest extends HandlerTest {
	// the test subject
	private UdpDataHandler handler;

	@Mock
	private UdpConnection connection;

	@BeforeEach
	void setUp() {
		handler = new UdpDataHandler();
	}

	@Test
	void handle() {
		// GIVEN
		givenFullContext();
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		given(ctx.incomingBuffer()).willReturn(buffer);
		given(connMgr.dataReceived(CSI, SSI,1024)).willReturn(connection);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementDataCount();
		verify(connMgr).dataReceived(CSI, SSI, 1024);

		verify(listener1).notifyDataAvailable(connection, buffer);
		verify(listener2).notifyDataAvailable(connection, buffer);
	}

	@Test
	void handle_connectionNotFound() {
		// GIVEN
		givenFullContext();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		given(ctx.incomingBuffer()).willReturn(buffer);
		given(connMgr.dataReceived(CSI, SSI,1024)).willReturn(connection);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementDataCount();
		verify(connMgr).dataReceived(CSI, SSI, 1024);

		verify(listener1, never()).notifyDataAvailable(connection, buffer);
		verify(listener2, never()).notifyDataAvailable(connection, buffer);
	}


	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}
}