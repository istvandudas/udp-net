package org.net.endpoint.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidatorUtilsTest {

	@Test
	void testRequiresValueBetweenValid() {
		// GIVEN
		int value = 5;

		// WHEN
		int result = ValidatorUtils.requiresValueBetween(value, 1, 10);

		// THEN
		assertThat(result).isEqualTo(5);
	}

	@Test
	void testRequiresValueBetweenBelowMinThrows() {
		// GIVEN
		int value = 0;

		// WHEN / THEN
		assertThatThrownBy(() -> ValidatorUtils.requiresValueBetween(value, 1, 10))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("between 1 and 10");
	}

	@Test
	void testRequiresValueBetweenAboveMaxThrows() {
		// GIVEN
		int value = 20;

		// WHEN / THEN
		assertThatThrownBy(() -> ValidatorUtils.requiresValueBetween(value, 1, 10))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("between 1 and 10");
	}

	@Test
	void testRequiresGreaterThanValid() {
		// GIVEN
		int value = 10;

		// WHEN
		int result = ValidatorUtils.requiresGreaterThan("test", value, 5);

		// THEN
		assertThat(result).isEqualTo(10);
	}

	@Test
	void testRequiresGreaterThanThrows() {
		// GIVEN
		int value = 5;

		// WHEN / THEN
		assertThatThrownBy(() -> ValidatorUtils.requiresGreaterThan("test", value, 5))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("greater than 5");
	}
}
