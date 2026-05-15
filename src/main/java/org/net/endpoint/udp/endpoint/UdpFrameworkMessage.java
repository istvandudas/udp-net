package org.net.endpoint.udp.endpoint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UdpFrameworkMessage {
	CreateConnection((byte) 0b000, "CREATE"),
	ConnectionAccepted((byte) 0b001, "ACCEPT"),
	ConnectionRejected((byte) 0b010, "REJECT"),
	ConnectionClosed((byte) 0b011, "CLOSE"),
	Heartbeat((byte) 0b100, "HEARTBEAT"),
	Data((byte) 0b101, "DATA");

	@Getter
	private final byte type;
	@Getter
	private final String shortName;

	private static final UdpFrameworkMessage[] TYPES = new UdpFrameworkMessage[UdpFrameworkMessage.values().length];

	static {
		for (UdpFrameworkMessage msgType : UdpFrameworkMessage.values()) {
			TYPES[msgType.type] = msgType;
		}
	}

	public static UdpFrameworkMessage byType(int type) {
		return TYPES[type];
	}

}
