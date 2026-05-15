package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.net.endpoint.TestUtil;
import org.net.endpoint.common.TimeMachine;

public abstract class SenderReceiverTestBase {
	final TimeMachine timeMachine = new TimeMachine();
	UnreliableTestClient sender;
	UnreliableTestClient receiver;

	@BeforeEach
	void setUp() throws Exception {
		sender = new UnreliableTestClient(
				TestUtil.epConfig(TestUtil.TEST_PROFILE, "test-client", TestUtil.EPHEMERAL_PORT), timeMachine);
		sender.start().await();
		receiver = new UnreliableTestClient(
				TestUtil.epConfig(TestUtil.TEST_PROFILE, "test-server", TestUtil.EPHEMERAL_PORT), timeMachine);
		receiver.start().await();
	}

	@AfterEach
	void tearDown() throws Exception {
		receiver.stop().await();
		sender.stop().await();
	}

}
