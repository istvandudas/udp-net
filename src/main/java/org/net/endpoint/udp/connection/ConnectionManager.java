package org.net.endpoint.udp.connection;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.common.Murmur3;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.endpoint.UdpFramework;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.List;
import java.util.function.Supplier;

import static org.net.endpoint.common.ValidatorUtils.requiresValueBetween;

@Slf4j
public class ConnectionManager {
	private static final int MAX_INCOMING_CONNECTION_LIMIT = 500000;
	private static final int MAX_OUTGOING_CONNECTION_LIMIT = 100000;
	private final UdpFramework udpFramework;
	private final TimeMachine timeMachine;
	private final Supplier<UdpConnection> connectionSupplier;
	private final Supplier<PendingConnection> pendingConnectionSupplier;
	private final int maxIncomingConnectionCount;
	private final int maxOutgoingConnectionCount;

	private final Long2ObjectMap<PendingConnection> pending = new Long2ObjectOpenHashMap<>();
	private final ConnectionGroup outgoingConnections = new ConnectionGroup();
	private final ConnectionGroup incomingConnections = new ConnectionGroup();
	private final SecureRandom secureRandom = new SecureRandom();

	private final ConnectionManagerMetrics metrics = new ConnectionManagerMetrics();

	public ConnectionManager(
			@NonNull UdpFramework udpFramework,
			@NonNull TimeMachine timeMachine,
			@NonNull Supplier<UdpConnection> connectionSupplier,
			@NonNull Supplier<PendingConnection> pendingConnectionSupplier,
			int maxIncomingConnectionCount,
			int maxOutgoingConnectionCount
	) {
		this.udpFramework = udpFramework;
		this.timeMachine = timeMachine;
		this.connectionSupplier = connectionSupplier;
		this.pendingConnectionSupplier = pendingConnectionSupplier;
		this.maxIncomingConnectionCount = requiresValueBetween(maxIncomingConnectionCount, 0, MAX_INCOMING_CONNECTION_LIMIT);
		this.maxOutgoingConnectionCount = requiresValueBetween(maxOutgoingConnectionCount, 0, MAX_OUTGOING_CONNECTION_LIMIT);
	}

	public ConnectionManagerMetricsView metrics() {
		return metrics.view();
	}

	public void incomingConnections(List<UdpConnection> connections) {
		incomingConnections.connections(connections);
	}

	public void outgoingConnections(List<UdpConnection> connections) {
		outgoingConnections.connections(connections);
	}

	public PendingConnection createPendingOutgoingConnection(@NonNull InetSocketAddress address) {
		PendingConnection pendingConn = pendingConnectionSupplier.get();
		if (pendingConn != null) {
			pendingConn.address = address;
			secureRandom.nextBytes(pendingConn.csi);
			pending.put(Murmur3.hash64(pendingConn.csi), pendingConn);
			udpFramework.sendCreateConnection(address, pendingConn.csi);
			metrics.incrementPendingOutgoingConnectionCount();
		} else {
			metrics.incrementPendingConnectionFailedCount();
		}
		return pendingConn;
	}

	public UdpConnection createIncomingConnection(@NonNull InetSocketAddress address, @NonNull byte[] csi) {
		if (incomingConnections.size() >= maxIncomingConnectionCount) {
			metrics.incrementConnectionCreateFailedCount();
			udpFramework.sendConnectionRejected(address, csi);
			return null;
		}
		UdpConnection conn = connectionSupplier.get();
		if (conn == null) {
			metrics.incrementConnectionCreateFailedCount();
			return null;
		}
		conn.udpFramework = udpFramework;
		conn.connMgr = this;
		conn.address = address;
		System.arraycopy(csi, 0, conn.csi, 0, UdpFramework.SESSION_ID_SIZE);
		secureRandom.nextBytes(conn.ssi);
		conn.key = generateKey(conn.csi, conn.ssi);
		updateLastTimestamps(conn);

		incomingConnections.add(conn);
		udpFramework.sendConnectionAccepted(address, conn.csi, conn.ssi);
		return conn;
	}

	public void reconnectUpdate(@NonNull UdpConnection conn, @NonNull byte[] csi) {
		synchronized (conn.stateChangeLock()) {
			if (incomingConnections.remove(conn)) {
				conn.connMgr = this;
				conn.udpFramework = udpFramework;
				System.arraycopy(csi, 0, conn.csi, 0, UdpFramework.SESSION_ID_SIZE);
				secureRandom.nextBytes(conn.ssi);
				conn.key = generateKey(conn.csi, conn.ssi);
				incomingConnections.add(conn);
				udpFramework.sendConnectionAccepted(conn.address, conn.csi, conn.ssi);
			}
			else {
				metrics.incrementIncomingConnectionNotFoundCount();
			}
		}
	}

	public UdpConnection createOutgoingConnection(
			@NonNull InetSocketAddress address,
			@NonNull byte[] csi,
			@NonNull byte[] ssi
	) {
		if (outgoingConnections.size() >= maxOutgoingConnectionCount) {
			metrics.incrementConnectionCreateFailedCount();
			return null;
		}
		PendingConnection pendingConnection = pending.remove(Murmur3.hash64(csi));
		if (pendingConnection == null) {
			metrics.incrementPendingConnectionNotFoundCount();
			metrics.incrementConnectionCreateFailedCount();
			return null;
		}
		UdpConnection conn = connectionSupplier.get();
		if (conn == null) {
			metrics.incrementConnectionCreateFailedCount();
			return null;
		}
		conn.udpFramework = udpFramework;
		conn.connMgr = this;
		conn.address = address;
		System.arraycopy(csi, 0, conn.csi, 0, UdpFramework.SESSION_ID_SIZE);
		System.arraycopy(ssi, 0, conn.ssi, 0, UdpFramework.SESSION_ID_SIZE);
		conn.key = generateKey(conn.csi, conn.ssi);
		updateLastTimestamps(conn);
		outgoingConnections.add(conn);
		return conn;
	}

	private void updateLastTimestamps(UdpConnection conn) {
		long now = timeMachine.nanoNow();
		conn.lastReceivedTime(now);
		conn.lastSentTime(now);
	}

	public boolean closeConnection(@NonNull UdpConnection conn) {
		UdpConnection udpConn = incomingConnections.lookup(conn.key);
		if (udpConn != null) {
			udpFramework.sendConnectionClosed(udpConn.address, udpConn.csi, udpConn.ssi);
			return closeIncomingConnection(conn);
		}
		udpConn = outgoingConnections.lookup(conn.key);
		if (udpConn != null) {
			udpFramework.sendConnectionClosed(udpConn.address, udpConn.csi, udpConn.ssi);
			return closeOutgoingConnection(conn);
		}
		return false;
	}

	public boolean closeIncomingConnection(@NonNull UdpConnection conn) {
		if (incomingConnections.remove(conn)) {
			conn.release();
			metrics.incrementIncomingConnectionClosedCount();
			return true;
		}
		metrics.incrementIncomingConnectionNotFoundCount();
		return false;
	}

	public boolean closeOutgoingConnection(@NonNull UdpConnection conn) {
		if (outgoingConnections.remove(conn)) {
			conn.release();
			metrics.incrementOutgoingConnectionClosedCount();
			return true;
		}
		metrics.incrementOutgoingConnectionNotFoundCount();
		return false;
	}

	public boolean closePendingOutgoingConnection(@NonNull byte[] csi) {
		PendingConnection pendingConnection = pending.remove(Murmur3.hash64(csi));
		if (pendingConnection != null) {
			pendingConnection.release();
			return true;
		}
		metrics.incrementPendingConnectionNotFoundCount();
		return false;
	}

	public void heartbeatReceived(@NonNull InetSocketAddress sender, @NonNull byte[] csi, @NonNull byte[] ssi) {
		UdpConnection conn = incomingConnections.lookup(generateKey(csi, ssi));
		if (conn != null) {
			if (conn.address.getPort() != sender.getPort()) {
				log.warn("Port changed for csi:{}, ssi:{} (from:{} to:{})", csi, ssi, conn.address, sender);
				incomingConnections.updatePort(conn, sender);
			}
			conn.metrics.lastReceivedHeartbeatTime(timeMachine.nanoNow());
		}
		else {
			metrics.incrementIncomingConnectionNotFoundCount();
		}
	}

	private long generateKey(byte[] csi, byte[] ssi) {
		return Murmur3.hash128ToLong(csi, ssi);
	}

	public UdpConnection findIncoming(long key) {
		return incomingConnections.lookup(key);
	}

	public UdpConnection findIncoming(@NonNull InetSocketAddress address) {
		return incomingConnections.lookup(address);
	}

	public UdpConnection findIncoming(@NonNull byte[] csi, @NonNull byte[] ssi) {
		return incomingConnections.lookup(generateKey(csi, ssi));
	}

	public UdpConnection findOutgoing(long key) {
		return outgoingConnections.lookup(key);
	}

	public void sendHeartbeat(@NonNull UdpConnection conn) {
		udpFramework.sendHeartbeat(conn.address, conn.csi, conn.ssi);
		conn.metrics.incrementSentHeartbeatCount();
	}

	public UdpConnection dataReceived(byte[] csi, byte[] ssi, int dataSize) {
		UdpConnection connection = findIncoming(csi, ssi);
		if (connection != null) {
			connection.metrics.incrementReceivedPacketCount();
			connection.metrics.addReceivedBytes(dataSize);
		}
		return connection;

	}

}