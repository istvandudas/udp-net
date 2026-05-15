package org.net.endpoint.udp.sender.strategy.poll;

import lombok.NonNull;
import java.util.Queue;

import java.util.concurrent.locks.LockSupport;

public class ThreePhaseWaitStrategy<T> implements PollStrategy<T> {
	public static final int SPIN_COUNT = 64;
	public static final long PARK_NANOS = 200_000L;

	private final int spinCount;
	private final long parkNanos;

	public ThreePhaseWaitStrategy() {
		this(SPIN_COUNT, PARK_NANOS);
	}

	public ThreePhaseWaitStrategy(int spinCount, long parkNanos) {
		this.spinCount = spinCount;
		this.parkNanos = parkNanos;
	}

	@Override
	public T poll(@NonNull Queue<T> queue) {
		// busy spin phase
		for (int i = 0; i < spinCount; i++) {
			T item = queue.poll();
			if (item != null) {
				return item;
			}
			Thread.onSpinWait();
		}
		// fallback phase
		T item = queue.poll();
		if (item != null) {
			return item;
		}
		// park phase
		LockSupport.parkNanos(parkNanos);
		return queue.poll();
	}
}
