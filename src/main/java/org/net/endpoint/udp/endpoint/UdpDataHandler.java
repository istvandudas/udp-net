package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.concurrent.atomic.AtomicLong;

public class UdpDataHandler implements FrameworkMessageHandler {

	private static final AtomicLong SEQ = new AtomicLong(0);

	@Override
	public void handle(@NonNull HandlerContext ctx) {
		ctx.metrics().incrementDataCount();
		UdpConnection connection = ctx.connMgr().dataReceived(ctx.csi(), ctx.ssi(), ctx.incomingBuffer().remaining());
		if (connection != null) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyDataAvailable(connection, ctx.incomingBuffer());
			}
		}
	}
}
