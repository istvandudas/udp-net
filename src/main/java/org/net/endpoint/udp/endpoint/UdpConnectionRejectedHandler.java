package org.net.endpoint.udp.endpoint;

import lombok.NonNull;

public class UdpConnectionRejectedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		ctx.metrics().incrementConnectionRejectedCount();
		if (ctx.connMgr().closePendingOutgoingConnection(ctx.csi())) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyConnectionRejected(ctx.address());
			}
		}
	}
}
