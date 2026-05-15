package org.net.endpoint.udp.connection;

import org.net.endpoint.common.ObjectPool;

public class TestUdpConnection extends UdpConnection {
	public static final ObjectPool<TestUdpConnection> POOL = new ObjectPool<>(TestUdpConnection::new);

	private TestUdpConnection() {}

	public static TestUdpConnection create() {
		return POOL.borrow();
	}

	@Override
	public void release() {
		POOL.giveBack(this);
	}
}
