package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpConnectionRejectedHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.ConnectionRejected [csi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				ctx.idMapper().map(ctx.csi())
		));
		ctx.metrics().incrementConnectionRejectedCount();
		if (ctx.connMgr().closePendingOutgoingConnection(ctx.csi())) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyConnectionRejected(ctx.address());
			}
		}
	}
}
