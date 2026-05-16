package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpHeartbeatHandler implements FrameworkMessageHandler {
	@Override
	public void handle(@NonNull HandlerContext ctx) {
		ctx.connMgr().heartbeatReceived(ctx.address(), ctx.csi(), ctx.ssi());
	}
}
