package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.udp.connection.UdpConnection;

@Slf4j
public class UdpConnectionClosedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.ConnectionClosed [csi:%d, ssi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				ctx.idMapper().map(ctx.csi()),
				ctx.idMapper().map(ctx.ssi())
		));
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
