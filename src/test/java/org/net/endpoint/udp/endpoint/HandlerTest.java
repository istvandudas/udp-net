package org.net.endpoint.udp.endpoint;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mock;
import org.net.endpoint.EndpointListener;
import org.net.endpoint.common.SessionIdMapper;
import org.net.endpoint.udp.connection.ConnectionManager;

import java.net.InetSocketAddress;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.net.endpoint.TestUtil.CSI;
import static org.net.endpoint.TestUtil.SSI;

@Slf4j
public class HandlerTest {

	@Mock
	protected HandlerContext ctx;
	@Mock
	protected InetSocketAddress address;
	@Mock
	protected UdpEndpointMetrics metrics;
	@Mock
	protected ConnectionManager connMgr;
	@Mock
	protected EndpointListener listener1;
	@Mock
	protected EndpointListener listener2;
	@Mock
	protected SessionIdMapper idMapper;

	protected void givenFullNoMetricContext() {
		givenNoMetricContext();
		lenient().when(ctx.ssi()).thenReturn(SSI);
		if (log.isDebugEnabled()) {
			lenient().when(idMapper.map(SSI)).thenReturn(22);
		}
	}

	protected void givenFullContext() {
		givenContext();
		lenient().when(ctx.ssi()).thenReturn(SSI);
		if (log.isDebugEnabled()) {
			lenient().when(idMapper.map(SSI)).thenReturn(22);
		}
	}

	protected void givenContext() {
		givenNoMetricContext();
		given(ctx.metrics()).willReturn(metrics);
	}

	private void givenNoMetricContext() {
		lenient().when(ctx.address()).thenReturn(address);
		lenient().when(ctx.connMgr()).thenReturn(connMgr);
		lenient().when(ctx.csi()).thenReturn(CSI);
		if (log.isDebugEnabled()) {
			lenient().when(idMapper.map(CSI)).thenReturn(11);
			lenient().when(ctx.name()).thenReturn("test-ep");
			lenient().when(ctx.idMapper()).thenReturn(idMapper);
			lenient().when(address.getHostName()).thenReturn("host");
			lenient().when(address.getPort()).thenReturn(1234);
		}
	}
}
