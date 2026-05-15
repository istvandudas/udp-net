package org.net.endpoint.maintenance.task;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.udp.connection.ConnectionManager;
import org.net.endpoint.udp.connection.ConnectionMetricsView;
import org.net.endpoint.udp.connection.UdpConnection;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class IdleConnectionCleanUpTaskTest {

	private static final long IDLE_TIMEOUT = Duration.ofSeconds(1).toNanos();
	private static final long NOW = Duration.ofSeconds(2).toNanos();

	// the test subject
	private IdleConnectionCleanUpTask task;

	@Mock
	private ConnectionManager connMgr;
	@Mock
	private EndpointListener listener1;
	@Mock
	private EndpointListener listener2;
	@Mock
	private UdpConnection conn;
	@Mock
	private ConnectionMetricsView metrics;

	@BeforeEach
	void setup() {
		task = new IdleConnectionCleanUpTask(connMgr, List.of(listener1, listener2), IDLE_TIMEOUT);
	}

	@Test
	void doesNothingWhenConnectionIsNotIdle() {
		// GIVEN
		long lastIncomingReceived = NOW - IDLE_TIMEOUT + 1;
		doAnswer(inv -> {
			List<UdpConnection> list = inv.getArgument(0);
			list.add(conn);
			return null;
		}).when(connMgr).incomingConnections(anyList());

		given(conn.metrics()).willReturn(metrics);
		given(metrics.lastReceivedTime()).willReturn(lastIncomingReceived);

		// WHEN
		task.execute(NOW);

		// THEN
		verify(listener1, never()).notifyConnectionGotBroken(any());
		verify(listener2, never()).notifyConnectionGotBroken(any());
		verify(connMgr, never()).closeIncomingConnection(any());
	}

	@Test
	void closesAndNotifiesWhenConnectionIsTimedOut() {
		// GIVEN
		long lastIncomingReceived = NOW - IDLE_TIMEOUT - 1;
		doAnswer(inv -> {
			List<UdpConnection> list = inv.getArgument(0);
			list.add(conn);
			return null;
		}).when(connMgr).incomingConnections(anyList());
		if (log.isDebugEnabled()) {
			given(conn.address()).willReturn(new InetSocketAddress("localhost", 5555));
		}
		given(conn.metrics()).willReturn(metrics);
		given(metrics.lastReceivedTime()).willReturn(lastIncomingReceived);

		// WHEN
		task.execute(NOW);

		// THEN
		verify(listener1).notifyConnectionGotBroken(conn);
		verify(listener2).notifyConnectionGotBroken(conn);
		verify(connMgr).closeIncomingConnection(conn);
	}

}