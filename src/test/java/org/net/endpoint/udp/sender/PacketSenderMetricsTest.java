package org.net.endpoint.udp.sender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.net.endpoint.udp.sender.PacketSenderMetrics.*;

class PacketSenderMetricsTest {

	// the test subject
	private PacketSenderMetrics metrics;

	@BeforeEach
	void setUp() {
		metrics = new PacketSenderMetrics(
				DEFAULT_HISTOGRAM_MAX_SENT_TIME,
				DEFAULT_HISTOGRAM_MAX_BATCH_SIZE,
				PacketSender.DEFAULT_QUEUE_CAPACITY
		);
	}

	@Test
	void constructor() {
		// GIVEN + WHEN + THEN
		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void constructor_invalid_histogramMaxSentTime() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE - 1,
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE + 1
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxSentTime must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void constructor_invalid_inclusive_histogramMaxSentTime() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE,
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE + 1
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxSentTime must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void constructor_invalid_histogramMaxBatchSize() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE - 1,
				LOWEST_DISCERNIBLE_VALUE + 1
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxBatchSize must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void constructor_invalid_inclusive_histogramMaxBatchSize() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE,
				LOWEST_DISCERNIBLE_VALUE + 1
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxBatchSize must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void constructor_invalid_histogramMaxQueueDepth() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE - 1
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxQueueDepth must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void constructor_invalid_inclusive_histogramMaxQueueDepth() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new PacketSenderMetrics(
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE + 1,
				LOWEST_DISCERNIBLE_VALUE
		))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("histogramMaxQueueDepth must be greater than " + LOWEST_DISCERNIBLE_VALUE + "!");
	}

	@Test
	void packetSent() {
		// GIVEN + WHEN
		metrics.recordPacketSent(10);
		metrics.recordPacketSent(20);

		// THEN
		assertThat(metrics.packetSentCount()).isEqualTo(2);

		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void packetSentPercentile() {
		// GIVEN + WHEN
		metrics.recordPacketSent(10);
		metrics.recordPacketSent(20);

		// THEN
		assertThat(metrics.packetSentPercentile(0.50d)).isEqualTo(10L);

		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void enqueuedPacketCount() {
		// GIVEN + WHEN
		metrics.incrementEnqueuedPacketCount();

		// THEN
		assertThat(metrics.enqueuedPacketCount()).isOne();

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void unsentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementUnsentPacketCount();

		// THEN
		assertThat(metrics.unsentPacketCount()).isOne();

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void partiallySentPacketCount() {
		// GIVEN + WHEN
		metrics.incrementPartiallySentPacketCount();

		// THEN
		assertThat(metrics.partiallySentPacketCount()).isOne();

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void batchBytesCount() {
		// GIVEN + WHEN
		metrics.recordBatchBytes(12);
		metrics.recordBatchBytes(22);

		// THEN
		assertThat(metrics.batchBytesCount()).isEqualTo(2);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void batchSizePercentile() {
		// GIVEN + WHEN
		metrics.recordBatchBytes(12);
		metrics.recordBatchBytes(22);

		// THEN
		assertThat(metrics.batchSizePercentile(0.50d)).isEqualTo(12);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void sentBytes() {
		// GIVEN + WHEN
		metrics.addSentBytes(42);

		// THEN
		assertThat(metrics.sentBytes()).isEqualTo(42);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void droppedBytes() {
		// GIVEN + WHEN
		metrics.addDroppedBytes(42);

		// THEN
		assertThat(metrics.droppedBytes()).isEqualTo(42);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void errorCount() {
		// GIVEN + WHEN
		metrics.incrementErrorCount();

		// THEN
		assertThat(metrics.errorCount()).isOne();

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

	@Test
	void queueDepthCount() {
		// GIVEN + WHEN
		metrics.recordQueueDepth(11);
		metrics.recordQueueDepth(22);

		// THEN
		assertThat(metrics.queueDepthCount()).isEqualTo(2);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
	}

	@Test
	void queueDepthPercentile() {
		// GIVEN + WHEN
		metrics.recordQueueDepth(11);
		metrics.recordQueueDepth(22);

		// THEN
		assertThat(metrics.queueDepthPercentile(0.50d)).isEqualTo(11);

		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
	}

	@Test
	void view() {
		// GIVEN + WHEN
		Object actual = metrics.view();

		// THEN
		assertThat(actual).isInstanceOf(PacketSenderMetricsView.class);
	}

	@Test
	void reset() {
		// GIVEN
		metrics.recordPacketSent(11);
		metrics.incrementEnqueuedPacketCount();
		metrics.incrementUnsentPacketCount();
		metrics.incrementPartiallySentPacketCount();
		metrics.recordBatchBytes(12);
		metrics.addSentBytes(42);
		metrics.addDroppedBytes(1);
		metrics.incrementErrorCount();
		metrics.recordQueueDepth(13);

		// WHEN
		metrics.reset();

		// THEN
		assertThat(metrics.packetSentCount()).isZero();
		assertThat(metrics.enqueuedPacketCount()).isZero();
		assertThat(metrics.unsentPacketCount()).isZero();
		assertThat(metrics.partiallySentPacketCount()).isZero();
		assertThat(metrics.batchBytesCount()).isZero();
		assertThat(metrics.sentBytes()).isZero();
		assertThat(metrics.droppedBytes()).isZero();
		assertThat(metrics.errorCount()).isZero();
		assertThat(metrics.queueDepthCount()).isZero();
	}

}