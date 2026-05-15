package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpHeartbeatHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.Heartbeat [csi:%d, ssi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				ctx.idMapper().map(ctx.csi()),
				ctx.idMapper().map(ctx.ssi())
		));
		ctx.connMgr().heartbeatReceived(ctx.address(), ctx.csi(), ctx.ssi());
	}
}
