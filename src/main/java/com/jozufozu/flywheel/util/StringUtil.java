package com.jozufozu.flywheel.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;

public class StringUtil {
	private static final NumberFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

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

	public static String args(String functionName, Object... args) {

		return functionName + '(' + Arrays.stream(args)
				.map(Object::toString)
				.collect(Collectors.joining(", ")) + ')';
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

	@Nonnull
	public static String readToString(InputStream is) throws IOException {
		ByteBuffer bytebuffer = null;

		try {
			bytebuffer = readToBuffer(is);
			int i = bytebuffer.position();
			((Buffer) bytebuffer).rewind();
			return MemoryUtil.memASCII(bytebuffer, i);
		} finally {
			if (bytebuffer != null) {
				FlwMemoryTracker.freeBuffer(bytebuffer);
			}
		}
	}

	@Nonnull
	public static ByteBuffer readToBuffer(InputStream is) throws IOException {
		if (is instanceof FileInputStream fileinputstream) {
			return readFileInputStream(fileinputstream);
		} else {
			return readInputStream(is);
		}
	}

	@Nonnull
	private static ByteBuffer readInputStream(InputStream is) throws IOException {
		ByteBuffer bytebuffer = FlwMemoryTracker.mallocBuffer(8192);
		ReadableByteChannel readablebytechannel = Channels.newChannel(is);

		while (readablebytechannel.read(bytebuffer) != -1) {
			if (bytebuffer.remaining() == 0) {
				bytebuffer = FlwMemoryTracker.reallocBuffer(bytebuffer, bytebuffer.capacity() * 2);
			}
		}
		return bytebuffer;
	}

	@Nonnull
	private static ByteBuffer readFileInputStream(FileInputStream fileinputstream) throws IOException {
		FileChannel filechannel = fileinputstream.getChannel();
		ByteBuffer bytebuffer = FlwMemoryTracker.mallocBuffer((int) filechannel.size() + 1);

		while (filechannel.read(bytebuffer) != -1) {
		}
		return bytebuffer;
	}

	public static String repeatChar(char c, int n) {
		char[] arr = new char[n];

		Arrays.fill(arr, c);

		return new String(arr);
	}
}
