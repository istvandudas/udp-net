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

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpCreateConnectionHandlerTest extends HandlerTest {

	// the test subject
	private UdpCreateConnectionHandler handler;

	@Mock
	private UdpConnection existingConn;
	@Mock
	private UdpConnection newIncomingConn;

	@BeforeEach
	void setUp() {
		handler = new UdpCreateConnectionHandler();
	}

	@Test
	void handle() {
		// GIVEN
		givenContext();
		given(connMgr.findIncoming(address)).willReturn(null);
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));
		given(connMgr.createIncomingConnection(address, ctx.csi())).willReturn(newIncomingConn);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementCreateConnectionCount();
		verify(connMgr).createIncomingConnection(address, ctx.csi());
		verify(listener1).notifyConnectionCreated(newIncomingConn);
		verify(listener2).notifyConnectionCreated(newIncomingConn);
		verify(metrics, never()).incrementDuplicateCreateConnectionCount();
		verify(connMgr, never()).reconnectUpdate(any(), any());
	}

	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}

	@Test
	void handle_connectionDoesExist() {
		// GIVEN
		givenContext();
		given(connMgr.findIncoming(address)).willReturn(existingConn);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementCreateConnectionCount();
		verify(metrics).incrementDuplicateCreateConnectionCount();
		verify(connMgr).reconnectUpdate(existingConn, ctx.csi());

		verify(connMgr, never()).createIncomingConnection(any(), any());
		verify(listener1, never()).notifyConnectionCreated(any());
		verify(listener2, never()).notifyConnectionCreated(any());
	}

	@Test
	void handle_cannotCreateMoreIncomingConnection() {
		// GIVEN
		givenContext();
		given(connMgr.findIncoming(address)).willReturn(null);
		given(connMgr.createIncomingConnection(address, ctx.csi())).willReturn(null);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementCreateConnectionCount();
		verify(connMgr).createIncomingConnection(address, ctx.csi());

		verify(listener1, never()).notifyConnectionCreated(any());
		verify(listener2, never()).notifyConnectionCreated(any());

		verify(metrics, never()).incrementDuplicateCreateConnectionCount();
		verify(connMgr, never()).reconnectUpdate(any(), any());
	}
}