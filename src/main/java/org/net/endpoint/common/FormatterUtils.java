package org.net.endpoint.common;


import javax.annotation.processing.Generated;

public class FormatterUtils {
	@Generated("ignore")
	private FormatterUtils() {}

	public static String formatLong(long value, int minLength) {
		String formatted = formatLong(value);
		if (formatted.length() < minLength) {
			formatted = " ".repeat(minLength - formatted.length()) + formatted;
		}
		return formatted;
	}

	public static String formatLong(long value) {
		if (value == 0) return "0";
		boolean negative = value < 0;
		long abs = negative ? -value : value;
		StringBuilder sb = new StringBuilder();
		int digitCount = 0;
		while (abs > 0) {
			if (digitCount > 0 && digitCount % 3 == 0) {
				sb.append(' ');
			}
			sb.append((char) ('0' + abs % 10));
			abs /= 10;
			digitCount++;
		}
		if (negative) {
			sb.append('-');
		}
		return sb.reverse().toString();
	}

	public static String formatMemoryLong(long value) {
		if (value < 0) {
			return "0";
		}
		if (value < 1_000) return Long.toString(value);
		if (value < 1_000_000) return String.format("%.2fK", value / 1_000.0);
		if (value < 1_000_000_000) return String.format("%.2fM", value / 1_000_000.0);
		return String.format("%.2fB", value / 1_000_000_000.0);
	}
}
