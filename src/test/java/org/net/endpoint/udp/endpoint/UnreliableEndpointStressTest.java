package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.net.endpoint.Endpoint;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;

import java.time.Duration;

@Tag("stress")
class UnreliableEndpointStressTest {
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
	void startStop_1K() throws Exception {
		// GIVEN + WHEN + THEN
		for (int i = 0; i < 1000; i++) {
			endpoint.start().await();
			endpoint.stop().await();
		}
	}
}