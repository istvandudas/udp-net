package org.net.endpoint;

public interface FrameworkConnection {
	void sendCreateConnection();
	void sendConnectionAccepted();
	void sendConnectionRejected();
	void sendConnectionClosed();
	void sendHeartbeat();
	void sendAck();
}
