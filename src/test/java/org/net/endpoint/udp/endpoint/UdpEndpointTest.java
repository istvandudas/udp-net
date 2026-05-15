package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.Test;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.TestUtil;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.maintenance.task.BrokenConnectionCleanUpTask;
import org.net.endpoint.maintenance.task.HeartbeatSenderTask;
import org.net.endpoint.maintenance.task.IdleConnectionCleanUpTask;
import org.net.endpoint.maintenance.task.MaintenanceTask;
import org.net.endpoint.udp.connection.PendingConnection;
import org.net.endpoint.udp.connection.UdpConnection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.net.endpoint.TestUtil.ENDPOINT_NAME;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
class UdpEndpointTest {

	private static final long PARK_TIME = 100_000L;

	// the test subject
	private UdpEndpoint endpoint;

	private TimeMachine timeMachine;
	private Supplier<UdpConnection> udpConnectionSupplier;
	private Supplier<PendingConnection> pendingConnectionSupplier;
	private BufferPool bufferPool;

	@Test
	void construct_config_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TestUdpEndpoint(
				null,
				mock(TimeMachine.class),
				mock(Supplier.class),
				mock(Supplier.class),
				mock(BufferPool.class),
				PARK_TIME
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("Cannot invoke \"org.net.endpoint.udp.endpoint.EndpointConfig.name()\" because \"config\" is null");
	}

	@Test
	void construct_timeMachine_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TestUdpEndpoint(
				TestUtil.CONFIG_WITH_EPHEMERAL_PORT,
				null,
				mock(Supplier.class),
				mock(Supplier.class),
				mock(BufferPool.class),
				PARK_TIME
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("timeMachine is marked non-null but is null");
	}

	@Test
	void construct_udpConnectionSupplier_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TestUdpEndpoint(
				TestUtil.CONFIG_WITH_EPHEMERAL_PORT,
				mock(TimeMachine.class),
				null,
				mock(Supplier.class),
				mock(BufferPool.class),
				PARK_TIME
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("udpConnectionSupplier is marked non-null but is null");
	}

	@Test
	void construct_pendingConnectionSupplier_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TestUdpEndpoint(
				TestUtil.CONFIG_WITH_EPHEMERAL_PORT,
				mock(TimeMachine.class),
				mock(Supplier.class),
				null,
				mock(BufferPool.class),
				PARK_TIME
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("pendingConnectionSupplier is marked non-null but is null");
	}

	@Test
	void construct_bufferPool_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new TestUdpEndpoint(
				TestUtil.CONFIG_WITH_EPHEMERAL_PORT,
				mock(TimeMachine.class),
				mock(Supplier.class),
				mock(Supplier.class),
				null,
				PARK_TIME
		))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("bufferPool is marked non-null but is null");
	}

	@Test
	void registerListener() {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		EndpointListener listener = mock(EndpointListener.class);
		endpoint.registerListener(listener);

		// THEN
		assertThat(endpoint.listeners).contains(listener);
	}

	@Test
	void registerListener_listener_isNull() {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN + THEN
		assertThatThrownBy(() -> endpoint.registerListener(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("listener is marked non-null but is null");
	}

	@Test
	void metrics() {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		Object actual = endpoint.metrics();

		// THEN
		assertThat(actual).isInstanceOf(UdpEndpointMetricsView.class);
	}

	@Test
	void effectivePort_ephemeral() throws Exception {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN + THEN
		endpoint.start().await();

		// WHNE
		int actual = endpoint.effectivePort();
		endpoint.stop().await();

		// THEN
		assertThat(actual).isNotZero();
		assertThat(actual).isNotEqualTo(TestUtil.EPHEMERAL_PORT);
	}

	@Test
	void effectivePort_fix() throws Exception {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.FIX_PORT);

		// WHEN + THEN
		endpoint.start().await();

		// WHNE
		int actual = endpoint.effectivePort();
		endpoint.stop().await();

		// THEN
		assertThat(actual).isNotZero();
		assertThat(actual).isEqualTo(TestUtil.FIX_PORT);
	}

	@Test
	void internalServicesAreRunning() throws Exception {
		// GIVE
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		endpoint.start().await();

		// THEN
		assertThat(endpoint.isRunning()).isTrue();
		assertThat(endpoint.maintenanceScheduler.isRunning()).isTrue();
		assertThat(endpoint.sender.isRunning()).isTrue();

		endpoint.stop().await();
	}

	@Test
	void maintenance_BrokenConnectionCleanUpTask_enabledByDefault() throws Exception {
		// GIVE
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).anyMatch(o -> o instanceof BrokenConnectionCleanUpTask);
		endpoint.stop().await();
	}

	@Test
	void maintenance_diable_BrokenConnectionCleanUpTask() throws Exception {
		// GIVE
		endpoint = givenEndpoint(new EndpointConfig(
				ENDPOINT_NAME,
				TestUtil.HOST,
				TestUtil.EPHEMERAL_PORT,
				TestUtil.TEST_INCOMING_BUFFER_SIZE,
				TestUtil.TEST_MAINTENANCE_INTERVAL,
				0,
				TestUtil.TEST_IDLE_TIMEOUT,
				TestUtil.TEST_HEARTBEAT_INTERVAL,
				TestUtil.TEST_MAX_INCOMING_CONNECTION_COUNT,
				TestUtil.TEST_MAX_OUTGOING_CONNECTION_COUNT
		));

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).doesNotMatch(o -> o instanceof BrokenConnectionCleanUpTask);
		endpoint.stop().await();
	}

	@Test
	void maintenance_IdleConnectionCleanUpTask_enabledByDefault() throws Exception {
		// GIVE
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).anyMatch(o -> o instanceof IdleConnectionCleanUpTask);
		endpoint.stop().await();
	}

	@Test
	void maintenance_diable_IdleConnectionCleanUpTask() throws Exception {
		// GIVE
		endpoint = givenEndpoint(new EndpointConfig(
				ENDPOINT_NAME,
				TestUtil.HOST,
				TestUtil.EPHEMERAL_PORT,
				TestUtil.TEST_INCOMING_BUFFER_SIZE,
				TestUtil.TEST_MAINTENANCE_INTERVAL,
				TestUtil.TEST_HEARTBEAT_TIMEOUT,
				0,
				TestUtil.TEST_HEARTBEAT_INTERVAL,
				TestUtil.TEST_MAX_INCOMING_CONNECTION_COUNT,
				TestUtil.TEST_MAX_OUTGOING_CONNECTION_COUNT
		));

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).doesNotMatch(o -> o instanceof IdleConnectionCleanUpTask);
		endpoint.stop().await();
	}

	@Test
	void maintenance_HeartbeatSenderTask_enabledByDefault() throws Exception {
		// GIVE
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).anyMatch(o -> o instanceof HeartbeatSenderTask);
		endpoint.stop().await();
	}

	@Test
	void maintenance_diable_HeartbeatSenderTask() throws Exception {
		// GIVE
		endpoint = givenEndpoint(new EndpointConfig(
				ENDPOINT_NAME,
				TestUtil.HOST,
				TestUtil.EPHEMERAL_PORT,
				TestUtil.TEST_INCOMING_BUFFER_SIZE,
				TestUtil.TEST_MAINTENANCE_INTERVAL,
				TestUtil.TEST_HEARTBEAT_TIMEOUT,
				TestUtil.TEST_IDLE_TIMEOUT,
				0,
				TestUtil.TEST_MAX_OUTGOING_CONNECTION_COUNT,
				TestUtil.TEST_MAX_INCOMING_CONNECTION_COUNT
		));

		// WHEN
		endpoint.start().await();

		// THEN
		List<MaintenanceTask> tasks = new ArrayList<>();
		endpoint.maintenanceScheduler.scheduledTasks(tasks);
		assertThat(tasks).doesNotMatch(o -> o instanceof HeartbeatSenderTask);
		endpoint.stop().await();
	}

	@Test
	void connect() throws Exception {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);
		PendingConnection pendingConnection = PendingConnection.create();
		given(pendingConnectionSupplier.get()).willReturn(pendingConnection);
		given(timeMachine.nanoNow()).willReturn(10L);
		given(timeMachine.nanoElapsed(10L)).willReturn(10L, 5000L, 10000L, 15000L, 20000L);
		given(bufferPool.createForCmd()).willReturn(ByteBuffer.allocateDirect(BufferPool.CMD_BUFFER_SIZE));

		endpoint.start().await();

		// WHEN
		endpoint.connect(TestUtil.HOST, TestUtil.FIX_PORT);

		// THEN
		assertThat(endpoint.connMgr.metrics().pendingOutgoingConnectionCount()).isOne();
		endpoint.stop().await();
	}

	@Test
	void connect_onNotStartedEndpoint() {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN + THEN
		assertThatThrownBy(() -> endpoint.connect(TestUtil.HOST, TestUtil.FIX_PORT))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("The endpoint must be started first!");
	}

	@Test
	void connect_host_isNull() {
		// GIVEN
		endpoint = givenEndpoint(TestUtil.EPHEMERAL_PORT);

		// WHEN + THEN
		assertThatThrownBy(() -> endpoint.connect(null, TestUtil.FIX_PORT))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("host is marked non-null but is null");
	}

	private TestUdpEndpoint givenEndpoint(int port) {
		timeMachine = mock(TimeMachine.class);
		udpConnectionSupplier = mock(Supplier.class);
		pendingConnectionSupplier = mock(Supplier.class);
		bufferPool = mock(BufferPool.class);
		return new TestUdpEndpoint(
				TestUtil.epConfig(ENDPOINT_NAME, port),
				timeMachine,
				udpConnectionSupplier,
				pendingConnectionSupplier,
				bufferPool,
				PARK_TIME
		);
	}

	private TestUdpEndpoint givenEndpoint(EndpointConfig config) {
		timeMachine = mock(TimeMachine.class);
		udpConnectionSupplier = mock(Supplier.class);
		pendingConnectionSupplier = mock(Supplier.class);
		bufferPool = mock(BufferPool.class);
		return new TestUdpEndpoint(
				config,
				timeMachine,
				udpConnectionSupplier,
				pendingConnectionSupplier,
				bufferPool,
				PARK_TIME
		);
	}

}