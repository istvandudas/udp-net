package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.Endpoint;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

class UnreliableEndpointTest {
	// the test subject
	private Endpoint endpoint;

	@BeforeEach
	void setUp() {
		endpoint = new UnreliableUdpEndpoint(
				new EndpointConfig(
						"test",
						"localhost", 0,
						2048,
						Duration.ofMillis(1).toNanos(),
						Duration.ofMillis(9).toNanos(),
						Duration.ofMillis(3).toNanos(),
						Duration.ofMinutes(1).toNanos(),
						100,
						100
				),
				new TimeMachine(),
				new BufferPool()
		);
	}

	@Test
	void startStop() throws Exception {
		// GIVEN + WHEN + THEN
		endpoint.start().await();
		LockSupport.parkNanos(Duration.ofMillis(1).toNanos());
		endpoint.stop().await();
	}

}