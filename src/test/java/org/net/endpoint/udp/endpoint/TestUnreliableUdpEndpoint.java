package org.net.endpoint.udp.endpoint;

import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestUnreliableUdpEndpoint extends UnreliableUdpEndpoint {
	public TestUnreliableUdpEndpoint(EndpointConfig config, TimeMachine timeMachine, BufferPool bufferPool) {
		super(config, timeMachine, bufferPool);
	}

	public void setRunning(boolean running) {
		this.running.set(running);
	}

	public void setIncomingBuffer(ByteBuffer buffer) {
		this.incomingBuffer = buffer;
	}

	public void setChannel(UdpDatagramChannel channel) {
		this.channel = channel;
	}
}
