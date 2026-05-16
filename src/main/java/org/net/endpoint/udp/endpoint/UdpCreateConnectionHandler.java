package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.net.endpoint.Connection;
import org.net.endpoint.udp.connection.UdpConnection;

public class UdpCreateConnectionHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		ctx.metrics().incrementCreateConnectionCount();
		UdpConnection conn = ctx.connMgr().findIncoming(ctx.address());
		if (conn != null) {
			ctx.metrics().incrementDuplicateCreateConnectionCount();
			ctx.connMgr().reconnectUpdate(conn, ctx.csi());
			return;
		}
		Connection incomingConnection = ctx.connMgr().createIncomingConnection(ctx.address(), ctx.csi());
		if (incomingConnection != null) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyConnectionCreated(incomingConnection);
			}
		}
	}
}
