package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.Connection;
import org.net.endpoint.udp.connection.UdpConnection;

@Slf4j
public class UdpCreateConnectionHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.CreateConnection [csi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				ctx.idMapper().map(ctx.csi())
		));
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
