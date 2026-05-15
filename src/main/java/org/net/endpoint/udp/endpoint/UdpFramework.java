package org.net.endpoint.udp.endpoint;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.net.endpoint.common.BufferPool;
import org.net.endpoint.udp.sender.PacketSender;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.net.endpoint.udp.endpoint.UdpFrameworkMessage.*;

/**
 * <pre>
 *  Endpoint Framework Header Structure table:
 *  +-----+--------+------+-------------+---------------------+------------------------------------+
 *  | No. | Type   | Size | Reliability | Name                | Description                        |
 *  +-----+--------+------+-------------+---------------------+------------------------------------+
 *  |  1. | byte   |    1 | all         | ENDPOINT_DATAGRAM   | Endpoint Network Packet Identifier |
 *  |  2. | byte   |    1 | all         | flags               | Framework Header Flags             |
 *  |  3. | byte[] |   16 | all         | csi                 | Client Session id                  |
 *  |  4. | byte[] |   16 | all         | ssi                 | Server Session id                  |
 *  +-----+--------+------+-------------+---------------------+------------------------------------+
 *
 *  Framework Header Flags bits:
 *  +-----+-----------------------------------------------+
 *  | Bit | Description                                   |
 *  +-----+-----------------------------------------------+
 *  | 7-3 | reserved (not used)                           |
 *  | 2-0 | framework message type                        |
 *  |     | 0 0 0 - ConnectionRequest                     |
 *  |     | 0 0 1 - ConnectionAccept                      |
 *  |     | 0 1 0 - ConnectionReject                      |
 *  |     | 0 1 1 - ConnectionClose                       |
 *  |     | 1 0 0 - Data                                  |
 *  |     | 1 0 1 - Heartbeat                             |
 *  |     | 1 1 0 - reserved (not used)                   |
 *  |     | 1 1 1 - reserved (not used)                   |
 *  +-----+-----------------------------------------------+
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public class UdpFramework {
	public static final int SESSION_ID_SIZE = 16;
	public static final byte ENDPOINT_DATAGRAM = 0x42;

	private static final int TYPE_MASK = 0b111;

	private final BufferPool bufferPool;
	private final PacketSender packetSender;

	public void sendCreateConnection(@NonNull InetSocketAddress address, @NonNull byte[] csi) {
		ByteBuffer buffer = createConnection(csi);
		if (buffer != null) {
			packetSender.send(buffer, address, true);
		}
	}

	public void sendConnectionAccepted(@NonNull InetSocketAddress address, @NonNull byte[] csi, @NonNull byte[] ssi) {
		ByteBuffer buffer = connectionAccepted(csi, ssi);
		if (buffer != null) {
			packetSender.send(buffer, address, true);
		}
	}

	public void sendConnectionRejected(@NonNull InetSocketAddress address, @NonNull byte[] csi) {
		ByteBuffer buffer = connectionRejected(csi);
		if (buffer != null) {
			packetSender.send(buffer, address, true);
		}
	}

	public void sendConnectionClosed(@NonNull InetSocketAddress address, @NonNull byte[] csi, @NonNull byte[] ssi) {
		ByteBuffer buffer = connectionClosed(csi, ssi);
		if (buffer != null) {
			packetSender.send(buffer, address, true);
		}
	}

	public void sendHeartbeat(@NonNull InetSocketAddress address, @NonNull byte[] csi, @NonNull byte[] ssi) {
		ByteBuffer buffer = heartbeat(csi, ssi);
		if (buffer != null) {
			packetSender.send(buffer, address, true);
		}
	}

	public int sendData(
			@NonNull InetSocketAddress address,
			@NonNull byte[] csi,
			@NonNull byte[] ssi,
			@NonNull ByteBuffer buffer
	) {
		ByteBuffer frmBuffer = data(csi, ssi);
		if (frmBuffer == null) {
			return 0;
		}
		int available = frmBuffer.capacity() - frmBuffer.position();
		if (available < buffer.remaining()) {
			log.error("Too big data! ({}>{})", buffer.remaining(), available);
			return 0;
		}
		frmBuffer.put(buffer);
		frmBuffer.flip();
		int size = frmBuffer.limit();
		if (packetSender.send(frmBuffer, address, true)) {
			return size;
		} else {
			bufferPool.release(frmBuffer);
			return 0;
		}
	}

	private ByteBuffer createConnection(byte[] csi) {
		ByteBuffer buffer = bufferPool.createForCmd();
		if (buffer == null) return null;
		writeHeader(buffer, csi, null, UdpFrameworkMessage.CreateConnection);
		buffer.flip();
		return buffer;
	}

	private ByteBuffer connectionAccepted(byte[] csi, byte[] ssi) {
		ByteBuffer buffer = bufferPool.createForCmd();
		if (buffer == null) return null;
		writeHeader(buffer, csi, ssi, UdpFrameworkMessage.ConnectionAccepted);
		buffer.flip();
		return buffer;
	}

	private ByteBuffer connectionRejected(byte[] csi) {
		ByteBuffer buffer = bufferPool.createForCmd();
		if (buffer == null) return null;
		writeHeader(buffer, csi, null, ConnectionRejected);
		buffer.flip();
		return buffer;
	}

	private ByteBuffer connectionClosed(byte[] csi, byte[] ssi) {
		ByteBuffer buffer = bufferPool.createForCmd();
		if (buffer == null) return null;
		writeHeader(buffer, csi, ssi, ConnectionClosed);
		buffer.flip();
		return buffer;
	}

	private ByteBuffer heartbeat(byte[] csi, byte[] ssi) {
		ByteBuffer buffer = bufferPool.createForCmd();
		if (buffer == null) return null;
		writeHeader(buffer, csi, ssi, Heartbeat);
		buffer.flip();
		return buffer;
	}

	private ByteBuffer data(byte[] csi, byte[] ssi) {
		ByteBuffer buffer = bufferPool.createForData();
		if (buffer == null) return null;
		writeHeader(buffer, csi, ssi, Data);
		return buffer;
	}

	private void writeHeader(ByteBuffer buffer, byte[] csi, byte[] ssi, UdpFrameworkMessage udpFrameworkMessage) {
		buffer.put(ENDPOINT_DATAGRAM);
		buffer.put((byte) (udpFrameworkMessage.type() & TYPE_MASK));
		buffer.put(csi, 0, SESSION_ID_SIZE);
		if (CreateConnection != udpFrameworkMessage && ConnectionRejected != udpFrameworkMessage) {
			buffer.put(ssi, 0, SESSION_ID_SIZE);
		}
	}

	public UdpFrameworkMessage readHeader(@NonNull ByteBuffer buffer, @NonNull byte[] csi, @NonNull byte[] ssi) {
		if (buffer.get() != ENDPOINT_DATAGRAM) {
			return null;
		}
		byte flags = buffer.get();
		buffer.get(csi, 0, SESSION_ID_SIZE);
		UdpFrameworkMessage udpFrameworkMessage = UdpFrameworkMessage.byType(flags & TYPE_MASK);
		if (CreateConnection != udpFrameworkMessage && ConnectionRejected != udpFrameworkMessage) {
			buffer.get(ssi, 0, SESSION_ID_SIZE);
		}
		else {
			Arrays.fill(ssi, (byte) 0);
		}
		return udpFrameworkMessage;
	}
}