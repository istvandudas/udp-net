package org.net.endpoint.maintenance.task;

import lombok.RequiredArgsConstructor;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class HeartbeatSenderTask implements MaintenanceTask {
	private final ConnectionManager connMgr;
	private final long keepAlivePeriod;
	private final List<UdpConnection> connections = new ArrayList<>();

	@Override
	public void execute(long now) {
		connections.clear();
		connMgr.outgoingConnections(connections);
		for (int i = 0; i < connections.size(); i++) {
			UdpConnection conn = connections.get(i);
			if (now - conn.metrics().lastSentTime() > keepAlivePeriod) {
				connMgr.sendHeartbeat(conn);
			}
		}
	}
}
