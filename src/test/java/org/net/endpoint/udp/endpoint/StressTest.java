package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.AfterEach;
import org.net.endpoint.common.FormatterUtils;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.udp.connection.ConnectionMetricsView;
import org.net.endpoint.udp.connection.UdpConnection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.net.endpoint.TestUtil.HOST;

public class StressTest {
	final TimeMachine timeMachine = new TimeMachine();
	List<UnreliableTestClient> senders;
	UnreliableTestClient receiver;

	void setUp(int senderCount) throws Exception {
		senders = new ArrayList<>();
		for (int i = 0; i < senderCount; i++) {
			UnreliableTestClient sender = new UnreliableTestClient(
					new EndpointConfig(
							"stress-client-" + i,
							HOST,
							0,
							UdpEndpoint.SAFE_MTU_SIZE,
							Duration.ofSeconds(5).toNanos(),
							Duration.ofSeconds(20).toNanos(),
							Duration.ofMinutes(10).toNanos(),
							Duration.ofSeconds(3).toNanos(),
							1024,
							1024
					),
					timeMachine
			);
			sender.start().await();
			senders.add(sender);
		}
		receiver = new UnreliableTestClient(
				new EndpointConfig(
						"stress-server",
						HOST,
						0,
						UdpEndpoint.SAFE_MTU_SIZE,
						Duration.ofSeconds(5).toNanos(),
						Duration.ofSeconds(20).toNanos(),
						Duration.ofMinutes(10).toNanos(),
						Duration.ofSeconds(3).toNanos(),
						1024,
						1024
				),
				timeMachine
		);
		receiver.start().await();
	}

	@AfterEach
	void tearDown() throws Exception {
		for (int i = 0; i < senders.size(); i++) {
			System.out.println("Sender[" + i + "]." + senders.get(i).bufferPool().stat());
		}
		System.out.println("Receiver." + receiver.bufferPool().stat());

		receiver.stop().await();
		for (UnreliableTestClient sender : senders) {
			sender.stop().await();
		}
	}

	void printEndpointMetrics(String epName, UnreliableTestClient endpoint) {
		UdpEndpointMetricsView em = endpoint.metrics();
		System.out.println("  " + epName + " endpoint metrics:");
		System.out.println("    unknown packets:   " + FormatterUtils.formatLong(em.unknownPacketCount(), 16));
		System.out.println("    unknown bytes:     " + FormatterUtils.formatLong(em.unknownBytes(), 16) + " (+" + FormatterUtils.formatMemoryLong(em.unknownBytes()) + "+)");
		System.out.println("    incoming packets:  " + FormatterUtils.formatLong(em.incomingPacketCount(), 16));
		System.out.println("    incoming bytes:    " + FormatterUtils.formatLong(em.incomingBytes(), 16) + " (+" + FormatterUtils.formatMemoryLong(em.incomingBytes()) + "+)");
		System.out.println("    outgoing packets:  " + FormatterUtils.formatLong(em.outgoingPacketCount(), 16));
		System.out.println("    outgoing bytes:    " + FormatterUtils.formatLong(em.outgoingBytes(), 16) + " (+" + FormatterUtils.formatMemoryLong(em.outgoingBytes()) + "+)");
		System.out.println("    data count:        " + FormatterUtils.formatLong(em.dataCount(), 16));
		System.out.println("    data dropped:      " + FormatterUtils.formatLong(em.dataDroppedCount(), 16));

	}

	void printConnectionMetrics(UdpConnection conn) {
		ConnectionMetricsView cm = conn.metrics();
		System.out.println("    ----------------------------------------------");
		System.out.println("    connection metrics:");
		System.out.println("        last sent:                 " + cm.lastSentTime());
		System.out.println("        last received:             " + cm.lastReceivedTime());
		System.out.println("        sent packet count:        " + FormatterUtils.formatLong(cm.sentPacketCount(), 16));
		System.out.println("        sent bytes:               " + FormatterUtils.formatLong(cm.sentBytes(), 16) + " (+" + FormatterUtils.formatMemoryLong(cm.sentBytes()) + "+)");
		System.out.println("        received packet count:    " + FormatterUtils.formatLong(cm.receivedPacketCount(), 16));
		System.out.println("        received bytes:           " + FormatterUtils.formatLong(cm.receivedBytes(), 16) + " (+" + FormatterUtils.formatMemoryLong(cm.receivedBytes()) + "+)");
		System.out.println("        sent heartbeat count:     " + FormatterUtils.formatLong(cm.sentHeartbeatCount(), 16));
		System.out.println("        received heartbeat count: " + FormatterUtils.formatLong(cm.receivedHeartbeatCount(), 16));
		System.out.println("        last received heartbeat:   " + cm.lastReceivedHeartbeatTime());
	}


}
