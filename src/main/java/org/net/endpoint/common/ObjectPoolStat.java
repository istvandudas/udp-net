package org.net.endpoint.common;

import static org.net.endpoint.common.FormatterUtils.formatMemoryLong;

public record ObjectPoolStat(
		long create,
		long get,
		long hit,
		long back,
		long size
) {
	public String toString() {
		return String.join(",",
				formatMemoryLong(get) + "/" + formatMemoryLong(back),
				"c:" + formatMemoryLong(create),
				"h:" + formatMemoryLong(hit),
				"s:" + formatMemoryLong(size)
		);
	}
}
