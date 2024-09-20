package dev.engine_room.flywheel.lib.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtil {
	private static final NumberFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

	private StringUtil() {
	}

	public static int countLines(String s) {
		int lines = 1;
		int length = s.length();
		for (int i = 0; i < length; i++) {
			char c = s.charAt(i);
			if (c == '\n') {
				lines++;
			} else if (c == '\r') {
				lines++;
				if (i + 1 < length && s.charAt(i + 1) == '\n') {
					i++;
				}
			}
		}
		return lines;
	}

	public static String formatBytes(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		} else if (bytes < 1024 * 1024) {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f) + " KiB";
		} else if (bytes < 1024 * 1024 * 1024) {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f / 1024f) + " MiB";
		} else {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f / 1024f / 1024f) + " GiB";
		}
	}

	public static String formatTime(long ns) {
		if (ns < 1000) {
			return ns + " ns";
		} else if (ns < 1000000) {
			return THREE_DECIMAL_PLACES.format(ns / 1000f) + " μs";
		} else if (ns < 1000000000) {
			return THREE_DECIMAL_PLACES.format(ns / 1000000f) + " ms";
		} else {
			return THREE_DECIMAL_PLACES.format(ns / 1000000000f) + " s";
		}
	}

	public static String formatAddress(long address) {
		return "0x" + Long.toHexString(address);
	}

	public static String trimPrefix(String s, String prefix) {
		if (s.startsWith(prefix)) {
			return s.substring(prefix.length());
		} else {
			return s;
		}
	}

	public static String trimSuffix(String s, String prefix) {
		if (s.endsWith(prefix)) {
			return s.substring(0, s.length() - prefix.length());
		} else {
			return s;
		}
	}

	/**
	 * Copy of {@link String#indent(int)} with the trailing newline removed.
	 */
	public static String indent(String str, int n) {
		if (str.isEmpty()) {
			return "";
		}
		Stream<String> stream = str.lines();
		if (n > 0) {
			final String spaces = repeatChar(' ', n);
			stream = stream.map(s -> spaces + s);
		} else if (n == Integer.MIN_VALUE) {
			stream = stream.map(String::stripLeading);
		} else if (n < 0) {
			throw new IllegalArgumentException("Requested indentation (" + n + ") is unsupported");
		}
		return stream.collect(Collectors.joining("\n"));
	}

	public static String repeatChar(char c, int n) {
		char[] arr = new char[n];

		Arrays.fill(arr, c);

		return new String(arr);
	}
}
