package org.net.endpoint.udp.connection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ConnectionGroup {
	private final Object lockObject = new Object();
	private final Long2ObjectMap<UdpConnection> byKey = new Long2ObjectOpenHashMap<>();
	private final Object2ObjectMap<InetSocketAddress, UdpConnection> byAddress = new Object2ObjectOpenHashMap<>();

	public int size() {
		synchronized (lockObject) {
			return byKey.size();
		}
	}

	public UdpConnection lookup(long key) {
		synchronized (lockObject) {
			return byKey.get(key);
		}
	}

	public UdpConnection lookup(@NonNull InetSocketAddress address) {
		synchronized (lockObject) {
			return byAddress.get(address);
		}
	}

	public void add(UdpConnection conn) {
		synchronized (lockObject) {
			byKey.put(conn.key, conn);
			byAddress.put(conn.address(), conn);
		}
	}

	public boolean remove(@NonNull UdpConnection conn) {
		synchronized (lockObject) {
			UdpConnection removedConn = byKey.remove(conn.key);
			if (removedConn != null) {
				byAddress.remove(removedConn.address());
				return true;
			}
		}
		return false;
	}

	public void updatePort(@NonNull UdpConnection conn, @NonNull InetSocketAddress address) {
		synchronized (lockObject) {
			byAddress.remove(conn.address());
			conn.address = address;
			byAddress.put(address, conn);
		}
	}

	public void connections(@NonNull List<UdpConnection> collectorList) {
		synchronized (lockObject) {
			collectorList.addAll(byKey.values());
		}
	}

}
