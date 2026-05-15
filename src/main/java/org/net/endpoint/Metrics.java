package org.net.endpoint;

/**
 * Standardized interface for all metric implementations.
 *
 * <p>All metric getters/setters follow fluent naming conventions.</p>
 *
 * <p><strong>Counter metrics</strong></p>
 * <ul>
 *     <li>Names end with <code>Count</code></li>
 *     <li>Expose an <code>increment{MetricName}()</code> method to increase the counter</li>
 * </ul>
 *
 * <p><strong>Aggregator metrics</strong></p>
 * <ul>
 *     <li>Names can end with <code>Bytes|</code></li>
 *     <li>Expose an <code>add{MetricName}(long bytes)</code> method to accumulate byte values</li>
 * </ul>
 *
 * <p><strong>Time metrics</strong></p>
 * <ul>
 *     <li>Names end with <code>Time</code></li>
 *     <li>Expose a fluent setter <code>set{MetricName}(long time)</code> to override the stored time value</li>
 * </ul>
 *
 * <p><strong>Histogram metrics</strong></p>
 * <ul>
 *     <li>Names end with <code>Histogram</code></li>
 *     <li>Expose a <code>{metricName}Count()</code> getter returning the total count</li>
 *     <li>Expose a <code>{metricName}Percentile(double percent)</code> getter returning the percentile value</li>
 *     <li>Expose a <code>record{MetricName}(long value)</code> method to record histogram samples</li>
 * </ul>
 */
public interface Metrics {
	MetricsView view();
	void reset();
}

