package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.Connection;

@Slf4j
public class UdpConnectionAcceptedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.ConnectionAccepted [csi:%d, ssi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				ctx.idMapper().map(ctx.csi()),
				ctx.idMapper().map(ctx.ssi())
		));
		ctx.metrics().incrementConnectionAcceptedCount();
		Connection conn = ctx.connMgr().createOutgoingConnection(ctx.address(), ctx.csi(), ctx.ssi());
		if (conn != null) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyConnectionAccepted(conn);
			}
		}

	}
}
