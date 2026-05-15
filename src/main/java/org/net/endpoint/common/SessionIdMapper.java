package org.net.endpoint.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class SessionIdMapper {

	private final Map<ByteArrayWrapper, Integer> cache;
	private final AtomicInteger seq = new AtomicInteger(1);

	public SessionIdMapper(int maxSize) {
		this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<ByteArrayWrapper, Integer> eldest) {
				return size() > maxSize;
			}
		});
	}

	public int map(byte[] sessionId) {
		ByteArrayWrapper key = new ByteArrayWrapper(sessionId);

		Integer existing = cache.get(key);
		if (existing != null) {
			return existing;
		}

		int newId = seq.getAndIncrement();
		cache.put(key, newId);
		return newId;
	}

}
