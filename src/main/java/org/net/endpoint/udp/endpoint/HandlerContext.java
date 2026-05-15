package org.net.endpoint.udp.endpoint;

import lombok.Data;
import lombok.experimental.Accessors;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.common.SessionIdMapper;
import org.net.endpoint.udp.connection.ConnectionManager;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;

@Data
@Accessors(fluent = true)
public class HandlerContext {
	private final SessionIdMapper idMapper = new SessionIdMapper(100);
	private final String name;
	private final ConnectionManager connMgr;
	private final UdpEndpointMetrics metrics;
	private final byte[] csi = new byte[UdpFramework.SESSION_ID_SIZE];
	private final byte[] ssi = new byte[UdpFramework.SESSION_ID_SIZE];
	private InetSocketAddress address;
	private final List<EndpointListener> listeners;
	private final ByteBuffer incomingBuffer;
}
