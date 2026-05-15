package org.net.endpoint.udp.endpoint;

import lombok.Builder;

@Builder
public record EndpointConfig(
		String name,
		String host,
		int port,
		int incomingBufferSize,
		long maintenanceInterval,
		long heartbeatTimeout,
		long idleTimeout,
		long heartbeatInterval,
		int maxIncomingConnectionCount,
		int maxOutgoingConnectionCount
) {
}
