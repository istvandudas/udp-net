package org.net.endpoint.common;

import lombok.NonNull;

import java.time.Duration;

public class TokenBucket {
	private static final long TOKEN_NANOS = Duration.ofSeconds(1).toNanos();

	private final long capacityScaled;
	private long tokenScaled;

	private long refillPerSecond;
	private final TimeMachine timeMachine;
	private long lastRefill;
	private final Object lockObject = new Object();

	public TokenBucket(long capacity, long refillPerSecond, @NonNull TimeMachine timeMachine) {
		this.timeMachine = timeMachine;
		this.capacityScaled = capacity * TOKEN_NANOS;
		this.refillPerSecond = refillPerSecond;
		tokenScaled = capacityScaled;
		lastRefill = timeMachine.nanoNow();
	}

	public void refillPerSecond(long refillPerSecond) {
		synchronized (lockObject) {
			this.refillPerSecond = refillPerSecond;
		}
	}

	public boolean tryConsume() {
		synchronized (lockObject) {
			refill();
			if (tokenScaled >= TOKEN_NANOS) {
				tokenScaled -= TOKEN_NANOS;
				return true;
			}
		}
		return false;
	}

	private void refill() {
		long now = timeMachine.nanoNow();
		if (lastRefill == now) return;
		tokenScaled += (now - lastRefill) * refillPerSecond;
		if (tokenScaled > capacityScaled) tokenScaled = capacityScaled;
		lastRefill = now;
	}





}
