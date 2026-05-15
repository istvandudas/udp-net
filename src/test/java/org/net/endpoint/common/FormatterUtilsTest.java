package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FormatterUtilsTest {

	@Test
	void testNegativeReturnsZero() {
		// GIVEN
		long value = -5;

		// WHEN
		String result = FormatterUtils.formatMemoryLong(value);

		// THEN
		assertThat(result).isEqualTo("0");
	}

	@Test
	void testBelowThousand() {
		// GIVEN
		long value = 999;

		// WHEN
		String result = FormatterUtils.formatMemoryLong(value);

		// THEN
		assertThat(result).isEqualTo("999");
	}

	@Test
	void testThousandsFormatting() {
		// GIVEN
		long value = 12_345;

		// WHEN
		String result = FormatterUtils.formatMemoryLong(value);

		// THEN
		assertThat(result).isEqualTo("12.35K");
	}

	@Test
	void testMillionsFormatting() {
		// GIVEN
		long value = 5_678_901;

		// WHEN
		String result = FormatterUtils.formatMemoryLong(value);

		// THEN
		assertThat(result).isEqualTo("5.68M");
	}

	@Test
	void testBillionsFormatting() {
		// GIVEN
		long value = 3_456_789_012L;

		// WHEN
		String result = FormatterUtils.formatMemoryLong(value);

		// THEN
		assertThat(result).isEqualTo("3.46B");
	}

	@Test
	void testFormatLongZero() {
		assertThat(FormatterUtils.formatLong(0)).isEqualTo("0");
	}

	@Test
	void testFormatLongBelowThousand() {
		assertThat(FormatterUtils.formatLong(999)).isEqualTo("999");
	}

	@Test
	void testFormatLongThousands() {
		assertThat(FormatterUtils.formatLong(1_000)).isEqualTo("1 000");
	}

	@Test
	void testFormatLongMillions() {
		assertThat(FormatterUtils.formatLong(1_234_567)).isEqualTo("1 234 567");
	}

	@Test
	void testFormatLongNegative() {
		assertThat(FormatterUtils.formatLong(-1_234_567)).isEqualTo("-1 234 567");
	}

	@Test
	void testFormatLongWithMinLengthPadsLeadingSpaces() {
		assertThat(FormatterUtils.formatLong(1_234_567, 15)).isEqualTo("      1 234 567");
	}

	@Test
	void testFormatLongWithMinLengthNopadWhenLonger() {
		assertThat(FormatterUtils.formatLong(1_234_567, 3)).isEqualTo("1 234 567");
	}
}
