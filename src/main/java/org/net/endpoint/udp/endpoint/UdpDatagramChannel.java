package org.net.endpoint.udp.endpoint;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

@RequiredArgsConstructor
public class UdpDatagramChannel implements AutoCloseable {
	private final DatagramChannel channel;

	public InetSocketAddress receive(ByteBuffer dst) throws IOException {
		return (InetSocketAddress) channel.receive(dst);
	}

	public int send(ByteBuffer src, SocketAddress target) throws IOException {
		return channel.send(src, target);
	}

	@Override
	public void close() throws IOException {
		channel.close();
	}

	public SocketAddress getLocalAddress() throws IOException {
		return channel.getLocalAddress();
	}

	public void bind(SocketAddress local) throws IOException {
		channel.bind(local);
	}

	public void configureBlocking(boolean block) throws IOException {
		channel.configureBlocking(block);
	}
}
