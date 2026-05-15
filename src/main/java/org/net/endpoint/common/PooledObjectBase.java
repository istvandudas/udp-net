package org.net.endpoint.common;

public abstract class PooledObjectBase implements PooledObject {
	private volatile boolean inPool = false;
	private boolean firstTime = true;

	public boolean firstTime() {
		return firstTime;
	}

	final void onBorrow() {
		inPool = false;
	}

	final void onRelease() {
		if (inPool) {
			throw new IllegalStateException(this + " is already released!");
		}
		if (firstTime) {
			firstTime = false;
		}
		inPool = true;
	}
}