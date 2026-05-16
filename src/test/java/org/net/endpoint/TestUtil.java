package org.net.endpoint;

import org.net.endpoint.udp.endpoint.EndpointConfig;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public final class TestUtil {

	public static final String ENDPOINT_NAME = "test-ep";
	public static final String HOST = "localhost";

	public static final String TEST_PROFILE = "test";
	public static final String NORMAL_PROFILE = "normal";

	public static final int TEST_INCOMING_BUFFER_SIZE = 1024;
	public static final long TEST_MAINTENANCE_INTERVAL = Duration.ofMillis(100).toNanos();
	public static final long TEST_HEARTBEAT_INTERVAL = Duration.ofMillis(200).toNanos();
	public static final long TEST_HEARTBEAT_TIMEOUT = Duration.ofMillis(600).toNanos();
	public static final long TEST_IDLE_TIMEOUT = Duration.ofMillis(3000).toNanos();
	public static final int TEST_MAX_INCOMING_CONNECTION_COUNT = 2;
	public static final int TEST_MAX_OUTGOING_CONNECTION_COUNT = 2;

	public static final int NORMAL_INCOMING_BUFFER_SIZE = 2048;
	public static final long NORMAL_MAINTENANCE_INTERVAL = Duration.ofSeconds(1).toNanos();
	public static final long NORMAL_HEARTBEAT_INTERVAL = Duration.ofSeconds(5).toNanos();
	public static final long NORMAL_HEARTBEAT_TIMEOUT = Duration.ofSeconds(20).toNanos();
	public static final long NORMAL_IDLE_TIMEOUT = Duration.ofMinutes(15).toNanos();
	public static final int NORMAL_MAX_INCOMING_CONNECTION_COUNT = 200000;
	public static final int NORMAL_MAX_OUTGOING_CONNECTION_COUNT = 50000;

	public static final int EPHEMERAL_PORT = 0;
	public static final int FIX_PORT = 9999;

	public static final EndpointConfig CONFIG_WITH_EPHEMERAL_PORT = epConfig(ENDPOINT_NAME, EPHEMERAL_PORT);

	public static final EndpointConfig CONFIG_WITH_FIX_PORT = epConfig(ENDPOINT_NAME, FIX_PORT);

	private static final long TIMEOUT = Duration.ofSeconds(1).toNanos();

	public static final byte[] CSI = new byte[] {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
	public static final byte[] SSI = new byte[] {17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};

	public static EndpointConfig epConfig(String name, int port) {
		return epConfig(TEST_PROFILE, name, port);
	}

	public static EndpointConfig epConfig(String profile, String name, int port) {
		if (NORMAL_PROFILE.equals(profile)) {
			return normalProfile(name, port);
		}
		if (TEST_PROFILE.equals(profile)) {
			return testProfile(name, port);
		}
		return null;
	}


	private static EndpointConfig normalProfile(String name, int port) {
		return new EndpointConfig(
				name,
				HOST,
				port,
				NORMAL_INCOMING_BUFFER_SIZE,
				NORMAL_MAINTENANCE_INTERVAL,
				NORMAL_HEARTBEAT_TIMEOUT,
				NORMAL_IDLE_TIMEOUT,
				NORMAL_HEARTBEAT_INTERVAL,
				NORMAL_MAX_INCOMING_CONNECTION_COUNT,
				NORMAL_MAX_OUTGOING_CONNECTION_COUNT
		);
	}

	private static EndpointConfig testProfile(String name, int port) {
		return new EndpointConfig(
				name,
				HOST,
				port,
				TEST_INCOMING_BUFFER_SIZE,
				TEST_MAINTENANCE_INTERVAL,
				TEST_HEARTBEAT_TIMEOUT,
				TEST_IDLE_TIMEOUT,
				TEST_HEARTBEAT_INTERVAL,
				TEST_MAX_INCOMING_CONNECTION_COUNT,
				TEST_MAX_OUTGOING_CONNECTION_COUNT
		);
	}


	public static boolean waitFor(Supplier<Integer> actual, int expected) {
		return waitFor(actual, expected, TIMEOUT);
	}

	public static boolean waitFor(Supplier<Integer> actual, int expected, long timeout) {
		long start = System.nanoTime();
		while (actual.get() < expected && System.nanoTime() - start < timeout) {
			LockSupport.parkNanos(100_000L);
		}
		return actual.get() >= expected;
	}


}

