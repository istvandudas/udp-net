package org.net.endpoint;

import lombok.NonNull;

import java.util.concurrent.CountDownLatch;

/**
 * Represents an endpoint that provides framework-level support for communicating
 * with another endpoint. An endpoint may operate as a server, a client, or both
 * simultaneously—its role does not fundamentally change how it is used.

 * All incoming connections and their events are delivered through the
 * {@link EndpointListener}. When running in server mode, outbound connections
 * can be initiated via {@link #connect(String, int)}, and the resulting events
 * are likewise propagated through the listener.

 * More details on sending and receiving data are available in the {@link Connection} class.
 */
public interface Endpoint {
	/**
	 * Starts all threads required by the endpoint.
	 *
	 * @return a latch that reaches zero once the endpoint is fully initialized and running.
	 */
	CountDownLatch start() throws Exception;

	/**
	 * Shuts down all threads and resources used by the endpoint.
	 *
	 * @return a latch that reaches zero once the endpoint has fully stopped.
	 */
	CountDownLatch stop() throws Exception;

	/**
	 * Registers a listener to receive endpoint lifecycle and event callbacks.
	 *
	 * @param listener the listener to register
	 */
	void registerListener(@NonNull EndpointListener listener);

	/**
	 * Returns the actual port the endpoint is bound to.
	 *
	 * @return the effective local port
	 */
	int effectivePort();

	/**
	 * Provides a read-only view of the endpoint's metrics.
	 *
	 * @return the metrics view for this endpoint
	 */
	MetricsView metrics();

	/**
	 * Connects to a remote host using the specified address and port.
	 *
	 * @param host the target host name or IP address
	 * @param port the target port
	 */
	void connect(@NonNull String host, int port);

}
