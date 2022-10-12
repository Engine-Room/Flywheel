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

import javax.annotation.Nonnull;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.memory.FlwMemoryTracker;

public class StringUtil {

	private static final NumberFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

	public static int countLines(String s) {
		return (int) s.lines()
				.count();
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

	public static String trimEnd(String value) {
		int len = value.length();
		int st = 0;
		while ((st < len) && Character.isWhitespace(value.charAt(len - 1))) {
			len--;
		}
		return value.substring(0, len);
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
