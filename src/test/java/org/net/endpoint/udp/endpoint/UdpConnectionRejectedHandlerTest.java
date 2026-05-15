package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.net.endpoint.TestUtil.CSI;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpConnectionRejectedHandlerTest extends HandlerTest {

	// the test subject
	private UdpConnectionRejectedHandler handler;

	@BeforeEach
	void setUp() {
		handler = new UdpConnectionRejectedHandler();
	}

	@Test
	void handle() {
		// GIVEN
		givenContext();
		given(connMgr.closePendingOutgoingConnection(CSI)).willReturn(true);
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionRejectedCount();
		verify(connMgr).closePendingOutgoingConnection(CSI);

		verify(listener1).notifyConnectionRejected(address);
		verify(listener2).notifyConnectionRejected(address);
	}

	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}

	@Test
	void handle_cannotClosePendingConnection() {
		// GIVEN
		givenContext();
		given(connMgr.closePendingOutgoingConnection(CSI)).willReturn(false);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionRejectedCount();
		verify(connMgr).closePendingOutgoingConnection(CSI);

		verify(listener1, never()).notifyConnectionRejected(any());
		verify(listener2, never()).notifyConnectionRejected(any());
	}

}
