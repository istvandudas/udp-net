package org.net.endpoint;

import org.net.endpoint.common.PooledObject;
import org.net.endpoint.udp.connection.ConnectionMetricsView;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface Connection extends PooledObject {
	InetSocketAddress address();
	ConnectionMetricsView metrics();
	boolean close();
	int send(ByteBuffer buffer);
}
