package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.net.endpoint.Connection;

public class UdpConnectionAcceptedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		ctx.metrics().incrementConnectionAcceptedCount();
		Connection conn = ctx.connMgr().createOutgoingConnection(ctx.address(), ctx.csi(), ctx.ssi());
		if (conn != null) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyConnectionAccepted(conn);
			}
		}

	}
}
