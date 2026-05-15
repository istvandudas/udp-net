package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class UdpDataHandler implements FrameworkMessageHandler {

	private static final AtomicLong SEQ = new AtomicLong(0);

	@Override
	public void handle(@NonNull HandlerContext ctx) {
		log.atDebug().log(() -> String.format(
				"%s:%d --> %s In.Data.%d [csi:%d, ssi:%d]",
				ctx.address().getHostName(),
				ctx.address().getPort(),
				ctx.name(),
				SEQ.incrementAndGet(),
				ctx.idMapper().map(ctx.csi()),
				ctx.idMapper().map(ctx.ssi())
		));
		ctx.metrics().incrementDataCount();
		UdpConnection connection = ctx.connMgr().dataReceived(ctx.csi(), ctx.ssi(), ctx.incomingBuffer().remaining());
		if (connection != null) {
			for (int i = 0; i < ctx.listeners().size(); i++) {
				ctx.listeners().get(i).notifyDataAvailable(connection, ctx.incomingBuffer());
			}
		}
	}
}
