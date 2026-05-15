package org.net.endpoint.common;

import javax.annotation.processing.Generated;

public class ValidatorUtils {

	@Generated("ignore")
	private ValidatorUtils() {}

	public static int requiresValueBetween(int value, int min, int max) {
		if (value < min || value > max) {
			throw new IllegalArgumentException("Value must be between " + min + " and " + max + ", but was " + value);
		}
		return value;
	}

	public static int requiresGreaterThan(String name, int value, int min) {
		if (value <= min) {
			throw new IllegalArgumentException(name + " must be greater than " + min + ", but was " + value);
		}
		return value;
	}


}
