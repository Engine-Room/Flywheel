package com.jozufozu.flywheel.util;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StringUtil {

	public static String args(String functionName, Object... args) {

		return functionName + '(' + Arrays.stream(args)
				.map(Object::toString)
				.collect(Collectors.joining(", ")) + ')';
	}

	public static String trimEnd(String value) {
		int len = value.length();
		int st = 0;
		while ((st < len) && Character.isWhitespace(value.charAt(len - 1))) {
			len--;
		}
		return value.substring(0, len);
	}
}
