package org.net.endpoint.common;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.LockSupport;

@Slf4j
public class TimeMachine {

	public long now() {
		return System.currentTimeMillis();
	}

	public long nanoNow() {
		return System.nanoTime();
	}

	public long nanoElapsed(long from) {
		return nanoNow() - from;
	}

	public boolean timeout(long from, long timeout) {
		return nanoNow() - from >= timeout;
	}

	public void sleepRemainingNanos(long sleepNanos, long fromNanos) {
		long remaining = sleepNanos - (nanoNow() - fromNanos);
		if (remaining > 0) {
			LockSupport.parkNanos(remaining);
		}
	}
}
