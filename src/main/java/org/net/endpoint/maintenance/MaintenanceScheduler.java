package org.net.endpoint.maintenance;

import lombok.NonNull;
import org.net.endpoint.common.NetService;
import org.net.endpoint.common.TimeMachine;
import org.net.endpoint.maintenance.task.MaintenanceTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class MaintenanceScheduler extends NetService {
	private static final String NAME_POSTFIX = ".maintenance-scheduler";

	private final TimeMachine timeMachine;
	private final long tickIntervalNanos;
	private final List<MaintenanceTask> tasks = new ArrayList<>();
	protected final AtomicLong tick = new AtomicLong(0L);

	public MaintenanceScheduler(@NonNull String name, long tickIntervalNanos, @NonNull TimeMachine timeMachine) {
		super(name + NAME_POSTFIX, true);
		this.timeMachine = timeMachine;
		this.tickIntervalNanos = tickIntervalNanos;
	}

	public void scheduledTasks(List<MaintenanceTask> tasks) {
		synchronized (this.tasks) {
			tasks.addAll(this.tasks);
		}
	}

	public void addTask(MaintenanceTask task) {
		synchronized (tasks) {
			tasks.add(task);
		}
	}

	@Override
	protected void beforeStart() throws Exception {
		tick.set(0L);
	}

	@Override
	public void run() {
		while (running.get()) {
			long now = timeMachine.nanoNow();
			for (int i = 0; i < tasks.size(); i++) {
				tasks.get(i).execute(now);
			}
			tick.incrementAndGet();
			timeMachine.sleepRemainingNanos(tickIntervalNanos, now);
		}
	}

	public long tick() {
		return tick.get();
	}

}
