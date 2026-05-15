package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.net.endpoint.TestUtil.CSI;
import static org.net.endpoint.TestUtil.SSI;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpConnectionAcceptedHandlerTest extends HandlerTest {

	private UdpConnectionAcceptedHandler handler;

	@Mock
	private UdpConnection newConn;

	@BeforeEach
	void setUp() {
		handler = new UdpConnectionAcceptedHandler();
	}

	@Test
	void handle() {
		// GIVEN
		givenFullContext();
		given(connMgr.createOutgoingConnection(address, CSI, SSI)).willReturn(newConn);
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionAcceptedCount();
		verify(connMgr).createOutgoingConnection(address, CSI, SSI);

		verify(listener1).notifyConnectionAccepted(newConn);
		verify(listener2).notifyConnectionAccepted(newConn);
	}

	@Test
	void handle_cannotCreateMoreOutgoingConnections() {
		// GIVEN
		givenFullContext();
		given(connMgr.createOutgoingConnection(address, CSI, SSI)).willReturn(null);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionAcceptedCount();
		verify(connMgr).createOutgoingConnection(address, CSI, SSI);

		verify(listener1, never()).notifyConnectionAccepted(any());
		verify(listener2, never()).notifyConnectionAccepted(any());
	}

	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}
}
