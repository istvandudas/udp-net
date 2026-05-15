package org.net.endpoint.udp.connection;

import lombok.Data;
import lombok.experimental.Accessors;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.common.ObjectPool;
import org.net.endpoint.common.ObjectPoolStat;
import org.net.endpoint.common.PooledObjectBase;
import org.net.endpoint.udp.endpoint.UdpFramework;

import java.net.InetSocketAddress;
import java.util.Arrays;

@Data
@Accessors(fluent = true)
public class PendingConnection extends PooledObjectBase {
	private final static ObjectPool<PendingConnection> POOL = new ObjectPool<>(PendingConnection::new);
	final byte[] csi = new byte[UdpFramework.SESSION_ID_SIZE];
	InetSocketAddress address;

	private PendingConnection() {
	}

	public static PendingConnection create() {
		return POOL.borrow();
	}

	@Override
	public void release() {
		Arrays.fill(csi, (byte) 0);
		address = null;
		POOL.giveBack(this);
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof PendingConnection that)) return false;
		return Arrays.equals(csi, that.csi);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(Murmur3.hash64(csi));
	}

	public static String stat() {
		return "PendingConnection[" + POOL.stat() + "]";
	}

	public static ObjectPoolStat poolStat() {
		return POOL.stat();
	}

}
