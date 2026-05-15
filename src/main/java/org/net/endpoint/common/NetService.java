package org.net.endpoint.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@RequiredArgsConstructor
public abstract class NetService implements Runnable {
	protected final String name;
	protected final boolean daemon;
	protected final AtomicBoolean running = new AtomicBoolean(false);
	protected Thread thread;
	protected CountDownLatch startedLatch;
	protected CountDownLatch stoppedLatch;

	public CountDownLatch start() throws Exception {
		if (!running.compareAndSet(false, true)) {
			log.error("Can't start {}, already running!", name);
			return null;
		}
		startedLatch = new CountDownLatch(1);
		stoppedLatch = new CountDownLatch(1);
		running.set(true);
		beforeStart();
		thread = new Thread(this::serviceLoop, name);
		thread.setDaemon(daemon);
		thread.start();
		return startedLatch;
	}

	protected void beforeStart() throws Exception {};

	public CountDownLatch stop() throws Exception {
		if (!running.compareAndSet(true, false)) {
			log.error("Can't stop {}, already stopped!", name);
			return null;
		}
		LockSupport.unpark(thread);
		afterStop();
		return stoppedLatch;
	}

	protected void afterStop() throws Exception {};

	private void serviceLoop() {
		startedLatch.countDown();
		log.info("{} has been started.", name);
		run();
		log.info("{} has been stopped.", name);
		stoppedLatch.countDown();
	}

	public boolean isRunning() {
		return running.get();
	}

}