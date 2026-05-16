package org.net.endpoint.maintenance.task;

import lombok.RequiredArgsConstructor;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class BrokenConnectionCleanUpTask implements MaintenanceTask {
	private final ConnectionManager connMgr;
	private final List<EndpointListener> endpointListeners;
	private final long heartbeatTimeout;
	private final List<UdpConnection> unreliableConnections = new ArrayList<>();

	@Override
	public void execute(long now) {
		unreliableConnections.clear();
		connMgr.incomingConnections(unreliableConnections);
		for (int i = 0; i < unreliableConnections.size(); i++) {
			UdpConnection conn = unreliableConnections.get(i);
			if (now - conn.metrics().lastReceivedHeartbeatTime() >= heartbeatTimeout) {
				for (int l = 0; l < endpointListeners.size(); l++) {
					endpointListeners.get(l).notifyConnectionGotBroken(conn);
				}
				connMgr.closeIncomingConnection(conn);
			}
		}
	}
}
