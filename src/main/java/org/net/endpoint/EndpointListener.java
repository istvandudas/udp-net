package org.net.endpoint;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface EndpointListener {
	void notifyConnectionGotBroken(Connection conn);
	void notifyConnectionDropped(Connection conn);
	void notifyConnectionCreated(Connection conn);
	void notifyConnectionAccepted(Connection conn);
	void notifyConnectionRejected(InetSocketAddress addr);
	void notifyConnectionClosed(InetSocketAddress addr);
	void notifyDataAvailable(Connection conn, ByteBuffer buffer);
	void notifyConnectionReady(Connection conn);
}
