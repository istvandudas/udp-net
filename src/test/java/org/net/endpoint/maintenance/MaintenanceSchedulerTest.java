package org.net.endpoint.maintenance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.net.endpoint.TestUtil;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.maintenance.task.MaintenanceTask;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue")
class MaintenanceSchedulerTest {

	private TimeMachine timeMachine;
	private MaintenanceScheduler scheduler;

	@BeforeEach
	void setUp() {
		timeMachine = mock(TimeMachine.class);
		scheduler = new MaintenanceScheduler("test", 500_000L, timeMachine);
	}

	@Test
	void construct_name_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new MaintenanceScheduler(null, 1000, timeMachine))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("name is marked non-null but is null");
	}

	@Test
	void construct_timeMachine_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new MaintenanceScheduler("test", 1000, null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("timeMachine is marked non-null but is null");
	}


	@Test
	void addedTask_executed() throws Exception {
		// GIVEN
		MaintenanceTask task = mock(MaintenanceTask.class);
		given(timeMachine.nanoNow()).willReturn(123L);
		scheduler.addTask(task);
		scheduler.start().await();

		// WHEN
		TestUtil.waitFor(() -> (int)scheduler.tick(), 1);
		scheduler.stop().await();

		// THEN
		verify(task, atLeast(1)).execute(123L);
	}

	@Test
	void allAddedTask_executed() throws Exception {
		// GIVEN
		MaintenanceTask task1 = mock(MaintenanceTask.class);
		MaintenanceTask task2 = mock(MaintenanceTask.class);
		given(timeMachine.nanoNow()).willReturn(123L);
		scheduler.addTask(task1);
		scheduler.addTask(task2);
		scheduler.start().await();

		// WHEN
		TestUtil.waitFor(() -> (int)scheduler.tick(), 1);
		scheduler.stop().await();

		// THEN
		verify(task1, atLeast(1)).execute(123L);
		verify(task2, atLeast(1)).execute(123L);
	}
}
