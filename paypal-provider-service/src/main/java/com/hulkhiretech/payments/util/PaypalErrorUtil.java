package com.hulkhiretech.payments.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.StringJoiner;

import com.hulkhiretech.payments.paypal.PaypalError;

public class PaypalErrorUtil {
	
	public static String generateErrorMessage(PaypalError error) {
		if (error == null) {
			return null;
		}

		StringJoiner joiner = new StringJoiner(" | ");

		// Top-level fields (excluding details)
		Arrays.stream(PaypalError.class.getDeclaredFields())
		.filter(field -> !"details".equals(field.getName()))
		.forEach(field -> addIfNotNull(joiner, field.getName(), field, error));

		// Nested details
		if (error.getDetails() != null) {
			for (int i = 0; i < error.getDetails().size(); i++) {
				PaypalError.Detail detail = error.getDetails().get(i);
				String prefix = "details[" + i + "].";

				Arrays.stream(PaypalError.Detail.class.getDeclaredFields())
				.forEach(field -> addIfNotNull(joiner, prefix + field.getName(), field, detail));
			}
		}

		return joiner.toString();
	}

	private static void addIfNotNull(StringJoiner joiner, String key, Field field, Object target) {
		try {
			field.setAccessible(true);
			Object value = field.get(target);
			if (value != null) {
				joiner.add(key + ":" + value);
			}
		} catch (IllegalAccessException ignored) {
		}
	}

}
