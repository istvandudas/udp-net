package org.net.endpoint.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class ObjectPoolTest {
	private final Supplier<String> supplier = () -> "test";
	private ObjectPool<String> objectPool;

	@BeforeEach
	public void setup() {
		objectPool = new ObjectPool<>(supplier);
	}

	@Test
	public void construct_supplier_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ObjectPool<>(null, 1))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("supplier is marked non-null but is null");
	}

	@Test
	public void construct_capacity_tooSmall() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new ObjectPool<>(() -> "hello", 0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("capacity must be greater than 0, but was 0");
	}

	@Test
	public void shouldBorrowObjectsFromPool() {
		// GIVEN + WHEN
		String obj = objectPool.borrow();

		// THEN
		assertThat(obj).isNotNull();
		assertThat(objectPool.size()).isZero();
	}

	@Test
	public void pooledObjectResued() {
		// GIVEN
		String obj = supplier.get();
		objectPool.giveBack(obj);

		// WHEN
		String actual = objectPool.borrow();

		// THEN the returned object is the same as given back object
		assertThat(actual).isEqualTo(obj);
	}

	@Test
	public void exhaustedPoolBorrowReturnsWithNull() {
		// GIVEN
		ObjectPool<String> exhaustedPool = new ObjectPool<>(() -> "test", 2);

		// WHEN
		String first = exhaustedPool.borrow();
		String second = exhaustedPool.borrow();
		String third = exhaustedPool.borrow();

		// THEN
		assertThat(first).isNotNull();
		assertThat(second).isNotNull();
		assertThat(third).isNull();
	}

	@Test
	public void giveBack() {
		// GIVEN
		String obj = objectPool.borrow();

		// WHEN
		boolean actual = objectPool.giveBack(obj);

		// THEN
		assertThat(actual).isTrue();
	}

	@Test
	void giveBack_callsOnRelease_onPooledObjectBasedClasses() {
		// GIVEN
		ObjectPool<PooledObjectBase> pool = new ObjectPool<>(() -> mock(PooledObjectBase.class));
		PooledObjectBase obj = pool.borrow();

		// WHEN
		pool.giveBack(obj);

		// THEN
		verify(obj).onBorrow();
	}

	@Test
	void giveBack_unsuccessful() {
		// GIVEN
		ObjectPool<String> pool = new ObjectPool<>(supplier, 2);
		boolean res1 = pool.giveBack("test-1");
		boolean res2 = pool.giveBack("test-2");

		// WHEN
		boolean actual = pool.giveBack("test");

		// THEN
		assertThat(res1).isTrue();
		assertThat(res2).isTrue();
		assertThat(actual).isFalse();
	}

	@Test
	public void giveBack_null() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> objectPool.giveBack(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("obj is marked non-null but is null");
	}

	@Test
	void testBorrowReturnsNullWhenCasLoopExhausted() throws Exception {
		// GIVEN
		Supplier<Object> supplier = mock(Supplier.class);
		ObjectPool<Object> pool = new ObjectPool<>(supplier, 2);

		AtomicInteger createdSpy = Mockito.spy(pool.created());
		Field createdField = ObjectPool.class.getDeclaredField("created");
		createdField.setAccessible(true);
		createdField.set(pool, createdSpy);

		given(createdSpy.get()).willReturn(0);
		given(createdSpy.compareAndSet(anyInt(), anyInt())).willReturn(false);

		// WHEN
		Object result = pool.borrow();

		// THEN
		assertThat(result).isNull();
		verify(createdSpy, atLeastOnce()).compareAndSet(anyInt(), anyInt());
		verifyNoInteractions(supplier);
	}

	@Test
	void testBorrowCasFailsThenSucceeds() throws Exception {
		// GIVEN
		Supplier<Object> supplier = mock(Supplier.class);
		Object createdObj = new Object();
		given(supplier.get()).willReturn(createdObj);

		ObjectPool<Object> pool = new ObjectPool<>(supplier, 2);

		AtomicInteger createdSpy = Mockito.spy(pool.created());
		Field createdField = ObjectPool.class.getDeclaredField("created");
		createdField.setAccessible(true);
		createdField.set(pool, createdSpy);

		given(createdSpy.get()).willReturn(0);
		given(createdSpy.compareAndSet(0, 1))
				.willReturn(false)  // first CAS fails
				.willReturn(true);  // second CAS succeeds

		// WHEN
		Object result = pool.borrow();

		// THEN
		assertThat(result).isSameAs(createdObj);
		verify(createdSpy, times(2)).compareAndSet(0, 1);
		verify(supplier).get();
	}

}
