package org.net.endpoint.udp.endpoint;

import lombok.experimental.Delegate;
import org.net.endpoint.Connection;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.connection.UdpConnection;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class UnreliableTestClient implements EndpointListener {

	@Delegate
	private final UnreliableUdpEndpoint endpoint;
	private final TimeMachine timeMachine;

	List<UdpConnection> outgoingConnections = new ArrayList<>();
	List<UdpConnection> incomingConnections = new ArrayList<>();
	List<UdpConnection> brokenConnections = new ArrayList<>();
	List<UdpConnection> droppedConnections = new ArrayList<>();
	List<InetSocketAddress> rejectedConnections = new ArrayList<>();
	List<InetSocketAddress> closedConnections = new ArrayList<>();
	List<UdpConnection> readyConnections = new ArrayList<>();

	public UnreliableTestClient(EndpointConfig config, TimeMachine timeMachine) {
		endpoint = new UnreliableUdpEndpoint(config, timeMachine, new BufferPool());
		this.timeMachine = timeMachine;
		endpoint.registerListener(this);
	}

	@Override
	public void notifyConnectionGotBroken(Connection conn) {
		brokenConnections.add((UdpConnection) conn);
	}

	@Override
	public void notifyConnectionDropped(Connection conn) {
		droppedConnections.add((UdpConnection) conn);
	}

	@Override
	public void notifyConnectionCreated(Connection conn) {
		incomingConnections.add((UdpConnection) conn);
	}

	@Override
	public void notifyConnectionAccepted(Connection conn) {
		outgoingConnections.add((UdpConnection)conn);
	}

	@Override
	public void notifyConnectionRejected(InetSocketAddress addr) {
		rejectedConnections.add(addr);
	}

	@Override
	public void notifyConnectionClosed(InetSocketAddress addr) {
		closedConnections.add(addr);
	}

	@Override
	public void notifyDataAvailable(Connection conn, ByteBuffer buffer) {
	}

	@Override
	public void notifyConnectionReady(Connection conn) {
		readyConnections.add((UdpConnection) conn);
	}

	public BufferPool bufferPool() {
		return endpoint.bufferPool;
	}

}
