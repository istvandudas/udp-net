package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.net.endpoint.Connection;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.MemoryTrackerExtension;
import org.net.endpoint.common.FormatterUtils;
import org.net.endpoint.common.TokenBucket;
import org.net.endpoint.udp.connection.UdpConnection;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.net.endpoint.TestUtil.HOST;
import static org.net.endpoint.TestUtil.waitFor;

@ExtendWith(MemoryTrackerExtension.class)
@Tag("stress")
public class UnreliableUdpEndpointStressTest extends StressTest {

	record TestParams(int senderCount, long totalBytes, int networkSpeedMbps, int packetSize) {
		/**
		 * Returns the network speed in bytes per second.
		 */
		long networkSpeedBytesPerSecond() {
			return (long) networkSpeedMbps * 1_000_000L / 8;
		}

		@Override
		public String toString() {
			return "senders=" + senderCount +
					", totalBytes=" + totalBytes +
					", networkSpeed=" + networkSpeedMbps + "Mbps" +
					", packetSize=" + packetSize;
		}
	}

	static Stream<TestParams> testParams() {
		return Stream.of(
				// 1 sender, 100MB, 100Mbps, 64B packets
				new TestParams(1, 100_000_000L, 100, 64),
				new TestParams(1, 100_000_000L, 100, 128),
				new TestParams(1, 100_000_000L, 100, 256),
				new TestParams(1, 100_000_000L, 100, 512),
				new TestParams(1, 100_000_000L, 100, 1024),

				// 2 senders, 100MB, 100Mbps, 64B packets
				new TestParams(2, 100_000_000L, 100, 64),
				new TestParams(2, 100_000_000L, 100, 128),
				new TestParams(2, 100_000_000L, 100, 256),
				new TestParams(2, 100_000_000L, 100, 512),
				new TestParams(2, 100_000_000L, 100, 1024)
		);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("testParams")
	void send(TestParams params) throws Exception {
		setUp(params.senderCount());

		long bytesPerSender = params.totalBytes() / params.senderCount();
		long packetsPerSender = bytesPerSender / params.packetSize();
		long totalExpectedPackets = packetsPerSender * params.senderCount();

		// Rate limit is evenly shared between senders
		long totalPacketsPerSecond = params.networkSpeedBytesPerSecond() / params.packetSize();
		long packetsPerSecondPerSender = totalPacketsPerSecond / params.senderCount();

		AtomicLong receivedBytes = new AtomicLong(0);
		AtomicLong receivedPackets = new AtomicLong(0);

		receiver.registerListener(new EndpointListener() {
			@Override
			public void notifyConnectionGotBroken(Connection conn) {
			}

			@Override
			public void notifyConnectionDropped(Connection conn) {
			}

			@Override
			public void notifyConnectionCreated(Connection conn) {
			}

			@Override
			public void notifyConnectionAccepted(Connection conn) {
			}

			@Override
			public void notifyConnectionRejected(InetSocketAddress addr) {
			}

			@Override
			public void notifyConnectionClosed(InetSocketAddress addr) {
			}

			@Override
			public void notifyConnectionReady(Connection conn) {
			}

			@Override
			public void notifyDataAvailable(Connection conn, ByteBuffer buffer) {
				receivedBytes.addAndGet(buffer.limit());
				receivedPackets.incrementAndGet();
			}
		});

		// Connect all senders to the receiver
		for (UnreliableTestClient sender : senders) {
			sender.connect(HOST, receiver.effectivePort());
		}
		assertThat(waitFor(() -> receiver.incomingConnections.size(), params.senderCount())).isTrue();
		for (UnreliableTestClient sender : senders) {
			assertThat(waitFor(() -> sender.outgoingConnections.size(), 1)).isTrue();
		}

		// Launch one sending thread per sender
		List<Thread> senderThreads = new ArrayList<>();
		for (UnreliableTestClient sender : senders) {
			UdpConnection conn = sender.outgoingConnections.getFirst();
			TokenBucket rateLimiter = new TokenBucket(packetsPerSecondPerSender, packetsPerSecondPerSender, timeMachine);

			Thread t = Thread.ofVirtual().start(() -> {
				ByteBuffer payload = ByteBuffer.allocateDirect(params.packetSize());
				for (int i = 0; i < params.packetSize(); i++) {
					payload.put((byte) i);
				}
				payload.flip();
				for (long i = 0; i < packetsPerSender; i++) {
					while (!rateLimiter.tryConsume()) {
						LockSupport.parkNanos(1_000L);
					}
					payload.position(0);
					while (conn.send(payload) == 0) {
						LockSupport.parkNanos(10_000L);
					}
				}
			});
			senderThreads.add(t);
		}

		// WHEN
		long start = System.currentTimeMillis();
		for (Thread t : senderThreads) {
			t.join();
		}
		long end = System.currentTimeMillis();

		// THEN
		boolean completed = waitFor(() -> (int) receivedPackets.get(), (int) totalExpectedPackets, Duration.ofSeconds(60).toNanos());

		// --- Metrics summary ---

		long sentDataSize = totalExpectedPackets * params.packetSize;
		long sentHeaderSize = receivedBytes.get() - sentDataSize;

		System.out.println("\n=== Test Metrics: " + params + " ===");
		System.out.println("  Sending took:           " + (end - start) + " ms");
		System.out.println("  Sent packets:      " + FormatterUtils.formatLong(totalExpectedPackets, 12));
		System.out.println("  Sent data size:    " + FormatterUtils.formatLong(sentDataSize, 12) + " (" + FormatterUtils.formatMemoryLong(sentDataSize) + ")");
		System.out.println("  Sent header size:  " + FormatterUtils.formatLong(sentHeaderSize, 12) + " (" + FormatterUtils.formatMemoryLong(sentHeaderSize) + ")");
		System.out.println("  Received packets:  " + FormatterUtils.formatLong(receivedPackets.get(), 12));
		System.out.println("  Received bytes:    " + FormatterUtils.formatLong(receivedBytes.get(), 12) + " (" + FormatterUtils.formatMemoryLong(receivedBytes.get()) + ")");
		printEndpointMetrics("\nReceiver", receiver);
		printConnectionMetrics(receiver.incomingConnections.getFirst());
		for (int i = 0; i < senders.size(); i++) {
			printEndpointMetrics("\nSender[" + i + "]", senders.get(i));
			printConnectionMetrics(senders.get(i).outgoingConnections.getFirst());
		}
		System.out.println("\n=== End Metrics ===");

		assertThat(receivedPackets.get()).isGreaterThan(0);
		assertThat(completed).as("Expected all packets to be received").isTrue();
	}

}
