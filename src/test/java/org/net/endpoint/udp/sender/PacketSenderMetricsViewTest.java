package org.net.endpoint.udp.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PacketSenderMetricsViewTest {

	// the test subject
	private PacketSenderMetricsView view;

	private PacketSenderMetrics metrics;

	@BeforeEach
	void setUp() {
		metrics = new PacketSenderMetrics(10, 11, 12);
		view = metrics.view();
	}

	@Test
	void packetSentCount() {
		// GIVEN + WHEN
		metrics.recordPacketSent(42);

		// THEN
		assertThat(view.packetSentCount()).isOne();
	}

	@Test
	void packetSentPercentile() {
		// GIVEN + WHEN
		metrics.recordPacketSent(22);
		metrics.recordPacketSent(44);

		// THEN
		assertThat(view.packetSentPercentile(0.50d)).isEqualTo(10);
	}

	@Test
	void enqueuedPacketCount() {
		// GIVEN + WHEN
		metrics.incrementEnqueuedPacketCount();

		// THEN
		assertThat(view.enqueuedPacketCount()).isOne();
	}

	@Test
	void unsentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementUnsentPacketCount();

		// THEN
		assertThat(view.unsentPacketCount()).isOne();
	}

	@Test
	void partiallySentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementPartiallySentPacketCount();

		// THEN
		assertThat(view.partiallySentPacketCount()).isOne();
	}

	@Test
	void batchBytesCount() {
		// GIVEN + WHEN
		metrics.recordBatchBytes(42);

		// THEN
		assertThat(view.batchSizeCount()).isOne();
	}

	@Test
	void batchSizePercentile() {
		// GIVEN + WHEN
		metrics.recordBatchBytes(22);
		metrics.recordBatchBytes(44);

		// THEN
		assertThat(view.batchSizePercentile(0.50d)).isEqualTo(22);
	}

	@Test
	void sentBytes() {
		// GIVEN + WHEN
		metrics.addSentBytes(42);

		// THEN
		assertThat(view.sentBytes()).isEqualTo(42);
	}

	@Test
	void droppedBytes() {
		// GIVEN + WHEN
		metrics.addDroppedBytes(42);

		// THEN
		assertThat(view.droppedBytes()).isEqualTo(42);
	}

	@Test
	void errorCount() {
		// GIVEN + WHEN
		metrics.incrementErrorCount();

		// THEN
		assertThat(view.errorCount()).isOne();
	}

	@Test
	void queueDepthCount() {
		// GIVEN + WHEN
		metrics.recordQueueDepth(42);

		// THEN
		assertThat(view.queueDepthCount()).isOne();
	}

	@Test
	void queueDepthPercentile() {
		// GIVEN + WHEN
		metrics.recordQueueDepth(22);
		metrics.recordQueueDepth(44);

		// THEN
		assertThat(view.queueDepthPercentile(0.50d)).isEqualTo(22);
	}

}