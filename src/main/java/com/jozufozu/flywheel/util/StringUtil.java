package com.jozufozu.flywheel.util;

public class StringUtil {
	public static String trimEnd(String value) {
		int len = value.length();
		int st = 0;
		while ((st < len) && Character.isWhitespace(value.charAt(len - 1))) {
			len--;
		}
		return value.substring(0, len);
	}
}
