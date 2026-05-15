package org.net.endpoint.udp.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.net.endpoint.TestUtil.CSI;
import static org.net.endpoint.TestUtil.SSI;

@SuppressWarnings("DataFlowIssue")
@ExtendWith(MockitoExtension.class)
class UdpHeartbeatHandlerTest extends HandlerTest {

	// the test subject
	private UdpHeartbeatHandler handler;

	@BeforeEach
	void setUp() {
		handler = new UdpHeartbeatHandler();
	}

	@Test
	void handle() {
		// GIVEN
		givenFullNoMetricContext();

		// WHEN
		handler.handle(ctx);

		// THEN
		verify(connMgr).heartbeatReceived(address, CSI, SSI);
	}

	@Test
	void handle_ctx_isNull() {
		// GIVEN + WHEN + THEN
		assertThatThrownBy(() -> handler.handle(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("ctx is marked non-null but is null");
	}

}
