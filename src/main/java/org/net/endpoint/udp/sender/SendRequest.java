package org.net.endpoint.udp.sender;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.net.endpoint.common.ObjectPool;
import org.net.endpoint.common.ObjectPoolStat;
import org.net.endpoint.common.PooledObjectBase;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(fluent = true)
public class SendRequest extends PooledObjectBase {
	private final static ObjectPool<SendRequest> POOL = new ObjectPool<>(SendRequest::new);

	private ByteBuffer buffer;
	private SocketAddress target;
	private boolean releaseAfterSend;

	private SendRequest() {
	}

	public static SendRequest create() {
		return POOL.borrow();
	}

	public void set(ByteBuffer buffer, SocketAddress target, boolean release) {
		this.buffer = buffer;
		this.target = target;
		this.releaseAfterSend = release;
	}

	public int writeableSize() {
		return buffer != null ? buffer.remaining() : 0;
	}

	public void clear() {
		this.buffer = null;
		this.target = null;
		this.releaseAfterSend = false;
	}

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
