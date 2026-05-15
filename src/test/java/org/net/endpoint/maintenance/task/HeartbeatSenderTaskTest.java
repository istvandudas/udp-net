package org.net.endpoint.maintenance.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.ConnectionMetricsView;
import org.net.endpoint.udp.connection.UdpConnection;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeartbeatSenderTaskTest {

	private static final long KEEP_ALIVE_PERIOD = 10L;
	private static final long NOW = 2L;

	// the test subject
	private HeartbeatSenderTask task;

	@Mock
	private ConnectionManager connMgr;
	@Mock
	private UdpConnection conn;
	@Mock
	private ConnectionMetricsView metrics;

	@BeforeEach
	void setup() {
		task = new HeartbeatSenderTask(connMgr, KEEP_ALIVE_PERIOD);
	}

	@Test
	void doesNothingWhenConnectionIsNotIdle() {
		// GIVEN
		long lastSentTime = NOW - KEEP_ALIVE_PERIOD + 1;
		doAnswer(inv -> {
			List<UdpConnection> list = inv.getArgument(0);
			list.add(conn);
			return null;
		}).when(connMgr).outgoingConnections(anyList());
		given(conn.metrics()).willReturn(metrics);
		given(metrics.lastSentTime()).willReturn(lastSentTime);

		// WHEN
		task.execute(NOW);

		// THEN
		verify(connMgr, never()).sendHeartbeat(any());
	}

	@Test
	void closesAndNotifiesWhenConnectionIsTimedOut() {
		// GIVEN
		long lastSentTime = NOW - KEEP_ALIVE_PERIOD - 1;
		doAnswer(inv -> {
			List<UdpConnection> list = inv.getArgument(0);
			list.add(conn);
			return null;
		}).when(connMgr).outgoingConnections(anyList());
		given(conn.metrics()).willReturn(metrics);
		given(metrics.lastSentTime()).willReturn(lastSentTime);

		// WHEN
		task.execute(NOW);

		// THEN
		verify(connMgr).sendHeartbeat(conn);
	}
}