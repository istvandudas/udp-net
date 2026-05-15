package org.net.endpoint.udp.connection;

import lombok.NonNull;
import org.net.endpoint.Connection;
import org.net.endpoint.common.PooledObjectBase;
import org.net.endpoint.udp.endpoint.UdpFramework;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class UdpConnection extends PooledObjectBase implements Connection {
	private final Object stateChangeLock = new Object();
	final ConnectionMetrics metrics = new ConnectionMetrics();
	final ConnectionMetricsView metricsView = new ConnectionMetricsView(metrics);
	ConnectionManager connMgr;
	UdpFramework udpFramework;
	InetSocketAddress address;

	final byte[] csi = new byte[UdpFramework.SESSION_ID_SIZE];
	final byte[] ssi = new byte[UdpFramework.SESSION_ID_SIZE];
	long key;

	public InetSocketAddress address() {
		return address;
	}

	public void reset() {
		Arrays.fill(csi, (byte) 0);
		Arrays.fill(ssi, (byte) 0);
		key = 0;
		address = null;
		metrics.reset();
		udpFramework = null;
		connMgr = null;
	}

	public ConnectionMetricsView metrics() {
		return metricsView;
	}

	public int send(@NonNull ByteBuffer buffer) {
		int sentBytes = udpFramework.sendData(address, csi, ssi, buffer);
		metrics.incrementSentPacketCount();
		metrics.addSentBytes(sentBytes);
		return sentBytes;
	}

	public void natPortUpdate(@NonNull InetSocketAddress address) throws IllegalArgumentException {
		if (this.address == null) {
			throw new IllegalArgumentException("address need to be initialized first!");
		}
		if (!this.address.getHostName().equals(address.getHostName())) {
			throw new IllegalArgumentException("hostname doesn't match!");
		}
		if (this.address.getPort() != address.getPort()) {
			this.address = address;
		}
	}

	public void lastReceivedTime(long time) {
		metrics.lastReceivedTime(time);
		metrics.lastReceivedHeartbeatTime(time);
	}

	public void lastSentTime(long now) {
		metrics.lastSentTime(now);
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof UdpConnection that)) return false;
		return Arrays.equals(csi, that.csi) && Arrays.equals(ssi, that.ssi);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(key);
	}

	@Override
	public boolean close() {
		return connMgr.closeConnection(this);
	}

	public Object stateChangeLock() {
		return stateChangeLock;
	}
}
