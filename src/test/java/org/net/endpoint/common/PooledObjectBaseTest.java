package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PooledObjectBaseTest {

	// the test subject
	static class TestPooledObject extends PooledObjectBase {
		private static final ObjectPool<TestPooledObject> POOL = new ObjectPool<>(TestPooledObject::new);

		private TestPooledObject() {}

		@Override
		public void release() {
			POOL.giveBack(this);
		}

		public static TestPooledObject create() {
			return POOL.borrow();
		}
	}

	@Test
	void firstTime() {
		// GIVEN + WHEN
		TestPooledObject object = TestPooledObject.create();

		// THEN
		assertThat(object.firstTime()).isTrue();
	}

	@Test
	void doubleRelease() {
		// GIVEN
		TestPooledObject object = TestPooledObject.create();
		object.release();

		// WHEN + THEN
		assertThatThrownBy(object::release)
				.isInstanceOf(IllegalStateException.class)
				.hasMessageEndingWith(" is already released!");
	}
}