package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SessionIdMapperTest {

	@Test
	void testAssignsNewIdWhenNotPresent() {
		// GIVEN
		SessionIdMapper mapper = new SessionIdMapper(10);
		byte[] key = {1, 2, 3};

		// WHEN
		int id = mapper.map(key);

		// THEN
		assertThat(id).isEqualTo(1);
	}

	@Test
	void testReturnsExistingIdWhenPresent() {
		// GIVEN
		SessionIdMapper mapper = new SessionIdMapper(10);
		byte[] key = {9, 9, 9};
		int first = mapper.map(key);

		// WHEN
		int second = mapper.map(key);

		// THEN
		assertThat(second).isEqualTo(first);
	}

	@Test
	void testLRUEvictionRemovesOldest() {
		// GIVEN
		SessionIdMapper mapper = new SessionIdMapper(2);
		byte[] a = {1};
		byte[] b = {2};
		byte[] c = {3};

		mapper.map(a);
		mapper.map(b);

		// WHEN
		mapper.map(c);

		// THEN
		assertThat(mapper.map(a)).isEqualTo(4);
	}

	@Test
	void testConstructorCreatesWorkingMapper() {
		// GIVEN
		SessionIdMapper mapper = new SessionIdMapper(5);

		// WHEN
		int id = mapper.map(new byte[]{1});

		// THEN
		assertThat(id).isEqualTo(1);
	}

	@Test
	void testMapUsesCacheGetAndPut() throws Exception {
		// GIVEN
		@SuppressWarnings("unchecked")
		Map<ByteArrayWrapper, Integer> mockMap = mock(Map.class);
		SessionIdMapper mapper = new SessionIdMapper(1);

		var field = SessionIdMapper.class.getDeclaredField("cache");
		field.setAccessible(true);
		field.set(mapper, mockMap);

		byte[] key = {7};

		// WHEN
		mapper.map(key);

		// THEN
		verify(mockMap).get(any());
		verify(mockMap).put(any(), any());
	}
}