package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.udp.connection.UdpConnection;

public class UdpConnectionClosedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		long key = Murmur3.hash128ToLong(ctx.csi(), ctx.ssi());
		UdpConnection conn = ctx.connMgr().findIncoming(key);
		if (conn != null) {
			ctx.metrics().incrementConnectionClosedCount();
			if (ctx.connMgr().closeIncomingConnection(conn)) {
				for (int i = 0; i < ctx.listeners().size(); i++) {
					ctx.listeners().get(i).notifyConnectionClosed(ctx.address());
				}
			} else {
				ctx.metrics().incrementConnectionCloseDroppedCount();
			}
			return;
		}

		conn = ctx.connMgr().findOutgoing(key);
		if (conn != null) {
			ctx.metrics().incrementConnectionClosedCount();
			if (ctx.connMgr().closeOutgoingConnection(conn)) {
				for (int i = 0; i < ctx.listeners().size(); i++) {
					ctx.listeners().get(i).notifyConnectionClosed(ctx.address());
				}
			} else {
				ctx.metrics().incrementConnectionCloseDroppedCount();
			}
			return;
		}
		ctx.metrics().incrementConnectionCloseDroppedCount();
	}
}
