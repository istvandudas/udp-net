package org.net.endpoint.common;

import lombok.NonNull;

import java.nio.ByteBuffer;

public class BufferPool {
	public static final int CMD_BUFFER_SIZE = 64;
	public static final int DATA_BUFFER_SIZE = 1200; // safe MTU size for data packets
	private final ObjectPool<ByteBuffer> cmdPool;
	private final ObjectPool<ByteBuffer> dataPool;

	public BufferPool() {
		this(
				new ObjectPool<>(() -> ByteBuffer.allocateDirect(CMD_BUFFER_SIZE)),
				new ObjectPool<>(() -> ByteBuffer.allocateDirect(DATA_BUFFER_SIZE))
		);
	}

	public BufferPool(@NonNull ObjectPool<ByteBuffer> cmdPool, @NonNull ObjectPool<ByteBuffer> dataPool) {
		this.cmdPool = cmdPool;
		this.dataPool = dataPool;
	}

	public ByteBuffer createForCmd() {
		return cmdPool.borrow();
	}

	public ByteBuffer createForData() {
		return dataPool.borrow();
	}

	public void release(ByteBuffer buffer) {
		if (buffer.capacity() == CMD_BUFFER_SIZE) {
			buffer.clear();
			cmdPool.giveBack(buffer);
			return;
		}
		if (buffer.capacity() == DATA_BUFFER_SIZE) {
			buffer.clear();
			dataPool.giveBack(buffer);
			return;
		}
		throw new IllegalArgumentException("Foreign buffer! (" + buffer.capacity() + ")");
	}

	public String stat() {
		return "BufferPool[cmdPool=" + cmdPool.stat() + ", dataPool=" + dataPool.stat() + "]";
	}

}
