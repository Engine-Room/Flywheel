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

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.backend.memory.FlwMemoryTracker;

public class StringUtil {

	private static final NumberFormat THREE_DECIMAL_PLACES = new DecimalFormat("#0.000");

	public static String formatBytes(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		} else if (bytes < 1024 * 1024) {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f) + " KB";
		} else if (bytes < 1024 * 1024 * 1024) {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f / 1024f) + " MB";
		} else {
			return THREE_DECIMAL_PLACES.format(bytes / 1024f / 1024f / 1024f) + " GB";
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

	public static String readToString(InputStream is) {
		ByteBuffer bytebuffer = null;

		try {
			bytebuffer = readToBuffer(is);
			int i = bytebuffer.position();
			((Buffer) bytebuffer).rewind();
			return MemoryUtil.memASCII(bytebuffer, i);
		} catch (IOException ignored) {
			//
		} finally {
			if (bytebuffer != null) {
				FlwMemoryTracker.freeBuffer(bytebuffer);
			}
		}

		return null;
	}

	public static ByteBuffer readToBuffer(InputStream is) throws IOException {
		if (is instanceof FileInputStream fileinputstream) {
			return readFileInputStream(fileinputstream);
		} else {
			return readInputStream(is);
		}
	}

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

	private static ByteBuffer readFileInputStream(FileInputStream fileinputstream) throws IOException {
		FileChannel filechannel = fileinputstream.getChannel();
		ByteBuffer bytebuffer = FlwMemoryTracker.mallocBuffer((int) filechannel.size() + 1);

		while (filechannel.read(bytebuffer) != -1) {
		}
		return bytebuffer;
	}
}
