package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
class BufferPoolTest {

	@Test
	void defaultConstructor() {
		// GIVEN
		BufferPool pool = new BufferPool();

		// WHEN
		ByteBuffer cmd = pool.createForCmd();
		ByteBuffer data = pool.createForData();

		// THEN
		assertThat(cmd.capacity()).isEqualTo(BufferPool.CMD_BUFFER_SIZE);
		assertThat(data.capacity()).isEqualTo(BufferPool.DATA_BUFFER_SIZE);
	}

	@Test
	void customConstructor() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);

		// WHEN
		pool.createForCmd();
		pool.createForData();

		// THEN
		verify(cmdPool).borrow();
		verify(mtuPool).borrow();
	}

	@Test
	void customConstructor_cmdPool_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new BufferPool(null, mock(ObjectPool.class)))
			.isInstanceOf(NullPointerException.class)
			.hasMessageContaining("cmdPool is marked non-null but is null");
	}

	@Test
	void customConstructor_dataPool_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> new BufferPool(mock(ObjectPool.class), null))
				.isInstanceOf(NullPointerException.class)
				.hasMessageContaining("dataPool is marked non-null but is null");
	}

	@Test
	void createForCmd_and_release() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		ByteBuffer cmdBuffer = ByteBuffer.allocateDirect(BufferPool.CMD_BUFFER_SIZE);
		when(cmdPool.borrow()).thenReturn(cmdBuffer);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);

		// WHEN
		ByteBuffer buf = pool.createForCmd();
		pool.release(buf);

		// THEN
		verify(cmdPool).borrow();
		verify(cmdPool).giveBack(cmdBuffer);
		verifyNoInteractions(mtuPool);
	}

	@Test
	void createForData_and_release() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		ByteBuffer dataBuffer = ByteBuffer.allocateDirect(BufferPool.DATA_BUFFER_SIZE);
		when(mtuPool.borrow()).thenReturn(dataBuffer);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);

		// WHEN
		ByteBuffer buf = pool.createForData();
		pool.release(buf);

		// THEN
		verify(mtuPool).borrow();
		verify(mtuPool).giveBack(dataBuffer);
		verifyNoInteractions(cmdPool);
	}

	@Test
	void releaseForeignBuffer() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);
		ByteBuffer foreign = ByteBuffer.allocateDirect(999);

		// WHEN + THEN
		assertThrows(IllegalArgumentException.class, () -> pool.release(foreign));
		verifyNoInteractions(cmdPool, mtuPool);
	}

	@Test
	void cmdPool_reusesReturnedBuffer() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		ByteBuffer cmdBuffer = ByteBuffer.allocateDirect(BufferPool.CMD_BUFFER_SIZE);
		when(cmdPool.borrow()).thenReturn(cmdBuffer);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);

		// WHEN
		ByteBuffer first = pool.createForCmd();
		pool.release(first);
		ByteBuffer second = pool.createForCmd();

		// THEN
		verify(cmdPool, times(2)).borrow();
		verify(cmdPool).giveBack(cmdBuffer);
		assertThat(second).isSameAs(cmdBuffer);
	}

	@Test
	void mtuPool_reusesReturnedBuffer() {
		// GIVEN
		ObjectPool<ByteBuffer> cmdPool = mock(ObjectPool.class);
		ObjectPool<ByteBuffer> mtuPool = mock(ObjectPool.class);
		ByteBuffer mtuBuffer = ByteBuffer.allocateDirect(BufferPool.DATA_BUFFER_SIZE);
		when(mtuPool.borrow()).thenReturn(mtuBuffer);
		BufferPool pool = new BufferPool(cmdPool, mtuPool);

		// WHEN
		ByteBuffer first = pool.createForData();
		pool.release(first);
		ByteBuffer second = pool.createForData();

		// THEN
		verify(mtuPool, times(2)).borrow();
		verify(mtuPool).giveBack(mtuBuffer);
		assertThat(second).isSameAs(mtuBuffer);
	}
}
