package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.net.endpoint.TestUtil.CSI;
import static org.net.endpoint.TestUtil.SSI;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpConnectionClosedHandlerTest extends HandlerTest {

	// the test subject
	private UdpConnectionClosedHandler handler;

	@Mock
	private UdpConnection incomingConn;
	@Mock
	private UdpConnection outgoingConn;

	@BeforeEach
	void setUp() {
		handler = new UdpConnectionClosedHandler();
	}

	@Test
	void handle_incomingConnectionClosed_success() {
		// GIVEN
		givenFullContext();
		long key = Murmur3.hash128ToLong(CSI, SSI);

		given(connMgr.findIncoming(key)).willReturn(incomingConn);
		given(connMgr.closeIncomingConnection(incomingConn)).willReturn(true);
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionClosedCount();
		verify(connMgr).closeIncomingConnection(incomingConn);

		verify(listener1).notifyConnectionClosed(address);
		verify(listener2).notifyConnectionClosed(address);

		verify(metrics, never()).incrementConnectionCloseDroppedCount();
	}

	@Test
	void handle_incomingConnectionClosed_failed() {
		// GIVEN
		givenFullContext();
		long key = Murmur3.hash128ToLong(CSI, SSI);

		given(connMgr.findIncoming(key)).willReturn(incomingConn);
		given(connMgr.closeIncomingConnection(incomingConn)).willReturn(false);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionClosedCount();
		verify(metrics).incrementConnectionCloseDroppedCount();

		verify(listener1, never()).notifyConnectionClosed(any());
		verify(listener2, never()).notifyConnectionClosed(any());
	}

	@Test
	void handle_outgoingConnectionClosed_success() {
		// GIVEN
		givenFullContext();
		long key = Murmur3.hash128ToLong(CSI, SSI);

		given(connMgr.findIncoming(key)).willReturn(null);
		given(connMgr.findOutgoing(key)).willReturn(outgoingConn);
		given(connMgr.closeOutgoingConnection(outgoingConn)).willReturn(true);
		given(ctx.listeners()).willReturn(List.of(listener1, listener2));

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionClosedCount();
		verify(connMgr).closeOutgoingConnection(outgoingConn);

		verify(listener1).notifyConnectionClosed(address);
		verify(listener2).notifyConnectionClosed(address);

		verify(metrics, never()).incrementConnectionCloseDroppedCount();
	}

	@Test
	void handle_outgoingConnectionClosed_failed() {
		// GIVEN
		givenFullContext();
		long key = Murmur3.hash128ToLong(CSI, SSI);

		given(connMgr.findIncoming(key)).willReturn(null);
		given(connMgr.findOutgoing(key)).willReturn(outgoingConn);
		given(connMgr.closeOutgoingConnection(outgoingConn)).willReturn(false);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionClosedCount();
		verify(metrics).incrementConnectionCloseDroppedCount();

		verify(listener1, never()).notifyConnectionClosed(any());
		verify(listener2, never()).notifyConnectionClosed(any());
	}

	@Test
	void handle_noConnectionFound() {
		// GIVEN
		givenFullContext();
		long key = Murmur3.hash128ToLong(CSI, SSI);

		given(connMgr.findIncoming(key)).willReturn(null);
		given(connMgr.findOutgoing(key)).willReturn(null);

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(metrics).incrementConnectionCloseDroppedCount();

		verify(listener1, never()).notifyConnectionClosed(any());
		verify(listener2, never()).notifyConnectionClosed(any());
	}

	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}
}
