package org.net.endpoint.common;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jctools.queues.varhandle.MpmcVarHandleArrayQueue;
import java.util.Queue;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Slf4j
@Getter
@Accessors(fluent = true)
public class ObjectPool<T> {

	private final Supplier<T> supplier;
	private final Queue<T> pool;

	private final AtomicInteger created = new AtomicInteger(0);

	private final AtomicLong getCount = new AtomicLong(0L);
	private final AtomicLong hitCount = new AtomicLong(0L);
	private final AtomicLong createCount = new AtomicLong(0L);
	private final AtomicLong giveBackCount = new AtomicLong(0L);

	private final int capacity;

	public ObjectPool(Supplier<T> supplier) {
		this(supplier, 4096);
	}

	public ObjectPool(@NonNull Supplier<T> supplier, int capacity) {
		ValidatorUtils.requiresGreaterThan("capacity", capacity, 0);
		this.capacity = capacity;
		this.supplier = supplier;
		this.pool = new MpmcVarHandleArrayQueue<>(capacity);
	}

	public int size() {
		return pool.size();
	}

	public T borrow() {
		getCount.incrementAndGet();
		T obj = pool.poll();
		if (obj != null) {
			hitCount.incrementAndGet();
			if (obj instanceof PooledObjectBase p) {
				p.onBorrow();
			}
			return obj;
		}
		for (int i = 0; i < 64; i++) {
			int current = created.get();
			if (current >= capacity) {
				return null;
			}
			if (created.compareAndSet(current, current + 1)) {
				T createdObj;
				createdObj = supplier.get();
				if (createdObj instanceof PooledObjectBase p) {
					p.onBorrow();
				}
				createCount.incrementAndGet();
				return createdObj;
			}
		}
		return null;
	}

	public boolean giveBack(@NonNull T obj) {
		if (pool.size() == capacity) {
			return false;
		}
		if (obj instanceof PooledObjectBase p) {
			p.onRelease();
		}
		pool.offer(obj);
		giveBackCount.incrementAndGet();
		return true;
	}

	public ObjectPoolStat stat() {
		return new ObjectPoolStat(createCount.get(), getCount.get(), hitCount.get(), giveBackCount.get(), pool.size());
	}
}
