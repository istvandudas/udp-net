package org.net.endpoint;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.net.endpoint.udp.connection.PendingConnection;
import org.net.endpoint.udp.connection.UnreliableUdpConnection;
import org.net.endpoint.udp.sender.SendRequest;

import java.lang.management.ManagementFactory;

import static org.net.endpoint.common.FormatterUtils.formatMemoryLong;

public class MemoryTrackerExtension implements BeforeEachCallback, AfterEachCallback {

	private long memoryBefore;
	private long heapBefore;
	private long nonHeapBefore;

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		System.gc();
		System.out.println("\nMemory used by " + context.getDisplayName() +
				"[mem: " + formatMemoryLong(usedMemory() - memoryBefore) +
				" heap: " + formatMemoryLong(usedHeap() - heapBefore) +
				" non-heap: " + formatMemoryLong(usedNonHeap() - nonHeapBefore) +
				"]"
		);
		System.out.println("  "+ PendingConnection.stat());
		System.out.println("  "+ UnreliableUdpConnection.stat());
		System.out.println("  "+ SendRequest.stat());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		System.gc();
		memoryBefore = usedMemory();
		heapBefore = usedHeap();
		nonHeapBefore = usedNonHeap();
	}

	private long usedMemory() {
		Runtime rt = Runtime.getRuntime();
		return rt.totalMemory() - rt.freeMemory();
	}

	private long usedHeap() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
	}

	private long usedNonHeap() {
		return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
	}

}
