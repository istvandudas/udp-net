package org.net.endpoint.maintenance.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class IdleConnectionCleanUpTask implements MaintenanceTask {
	private final ConnectionManager connMgr;
	private final List<EndpointListener> endpointListeners;
	private final long idleTimeout;
	private final List<UdpConnection> connections = new ArrayList<>();

	@Override
	public void execute(long now) {
		connections.clear();
		connMgr.incomingConnections(connections);
		for (int i = 0; i < connections.size(); i++) {
			UdpConnection conn = connections.get(i);
			long delta = now - conn.metrics().lastReceivedTime();
			if (delta >= idleTimeout) {
				if (log.isDebugEnabled()) {
					log.debug("Connection {}:{} has been disconnected. (idle: {} - {}, {} >= {})",
							conn.address().getHostName(),
							conn.address().getPort(),
							now,
							conn.metrics().lastReceivedHeartbeatTime(),
							delta,
							idleTimeout
					);
				}
				for (int l = 0; l < endpointListeners.size(); l++) {
					endpointListeners.get(l).notifyConnectionGotBroken(conn);
				}
				connMgr.closeIncomingConnection(conn);
			}
		}
	}
}
