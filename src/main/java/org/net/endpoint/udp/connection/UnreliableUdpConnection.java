package org.net.endpoint.udp.connection;

import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.common.ObjectPool;
import org.net.endpoint.common.ObjectPoolStat;

@Slf4j
public final class UnreliableUdpConnection extends UdpConnection {
	private static final ObjectPool<UnreliableUdpConnection> POOL = new ObjectPool<>(UnreliableUdpConnection::new);

	UnreliableUdpConnection() {
	}

	public static UdpConnection create() {
		return POOL.borrow();
	}

	@Override
	public void release() {
		reset();
		POOL.giveBack(this);
	}

	public static String stat() {
		return "UnreliableUdpConnection[" + POOL.stat() + "]";
	}

	public static ObjectPoolStat poolStat() {
		return POOL.stat();
	}

}
