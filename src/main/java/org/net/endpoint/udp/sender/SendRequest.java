package org.net.endpoint.udp.sender;

import lombok.Getter;
import lombok.ToString;
import org.net.endpoint.common.ObjectPool;
import org.net.endpoint.common.ObjectPoolStat;
import org.net.endpoint.common.PooledObjectBase;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

@Getter
@ToString
public class SendRequest extends PooledObjectBase {
	private final static ObjectPool<SendRequest> POOL = new ObjectPool<>(SendRequest::new);

	private ByteBuffer buffer;
	private SocketAddress target;
	private boolean release;

	private SendRequest() {
	}

	public static SendRequest create() {
		return POOL.borrow();
	}

	public void set(ByteBuffer buffer, SocketAddress target, boolean release) {
		this.buffer = buffer;
		this.target = target;
		this.release = release;
	}

	public int writeableSize() {
		return buffer != null ? buffer.remaining() : 0;
	}

	public void clear() {
		this.buffer = null;
		this.target = null;
		this.release = false;
	}

	@Override
	public void release() {
		clear();
		POOL.giveBack(this);
	}

	public static String stat() {
		return "SendRequest[" + POOL.stat() + "]";
	}

	public static ObjectPoolStat poolStat() {
		return POOL.stat();
	}
}
