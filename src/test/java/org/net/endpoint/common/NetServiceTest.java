package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;

class NetServiceTest {

	static class TestNetService extends NetService {

		private static final long IDLE_TIME = Duration.ofMillis(1).toNanos();

		CountDownLatch runLatch = new CountDownLatch(1);
		CountDownLatch beforeStartLatch = new CountDownLatch(1);
		CountDownLatch afterStoppedLatch = new CountDownLatch(1);

		public TestNetService() {
			super("test-net-service", false);
		}

		@Override
		protected void afterStop() throws Exception {
			afterStoppedLatch.countDown();
		}

		@Override
		protected void beforeStart() throws Exception {
			beforeStartLatch.countDown();
		}

		@Override
		public void run() {
			while (running.get()) {
				if (runLatch.getCount() > 0) {
					runLatch.countDown();
				}
				System.out.println(name + " is executed");
				LockSupport.parkNanos(IDLE_TIME);
			}
		}
	}

	@Test
	void canStartAndStop() throws Exception {
		// GIVEN
		TestNetService service = new TestNetService();

		// WHEN
		service.start().await();
		service.runLatch.await();

		// THEN
		service.stop().await();
	}

	@Test
	void doubleStart() throws Exception {
		// GIVEN
		TestNetService service = new TestNetService();
		service.start().await();
		service.runLatch.await();

		// WHEN
		CountDownLatch actual = service.start();

		// THEN
		assertThat(actual).isNull();
		service.stop().await();
	}

	@Test
	void stopIfNotStarted() throws Exception {
		// GIVEN
		TestNetService notStartedService = new TestNetService();

		// WHEN
		CountDownLatch actual = notStartedService.stop();

		// THEN
		assertThat(actual).isNull();
	}

	@Test
	void before() throws Exception {
		// GIVEN
		TestNetService service = new TestNetService();

		// WHEN
		service.start().await();
		service.runLatch.await();

		// THEN
		assertThat(service.beforeStartLatch.getCount()).isZero();

		// COOLDOWN
		service.stop().await();
	}

	@Test
	void after() throws Exception {
		// GIVEN
		TestNetService service = new TestNetService();
		service.start().await();
		service.runLatch.await();

		// WHEN
		service.stop().await();

		// THEN
		assertThat(service.afterStoppedLatch.getCount()).isZero();
	}

}