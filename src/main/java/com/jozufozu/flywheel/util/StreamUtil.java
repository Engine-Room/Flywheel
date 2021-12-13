package com.jozufozu.flywheel.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import org.lwjgl.system.MemoryUtil;

public class StreamUtil {
	public static String readToString(InputStream is) {
		ByteBuffer bytebuffer = null;

		try {
			bytebuffer = readToBuffer(is);
			int i = bytebuffer.position();
			((Buffer) bytebuffer).rewind();
			return MemoryUtil.memASCII(bytebuffer, i);
		} catch (IOException e) {

		} finally {
			if (bytebuffer != null) {
				MemoryUtil.memFree(bytebuffer);
			}

		}

		return null;
	}

	public static ByteBuffer readToBuffer(InputStream is) throws IOException {
		ByteBuffer bytebuffer;
		if (is instanceof FileInputStream) {
			FileInputStream fileinputstream = (FileInputStream) is;
			FileChannel filechannel = fileinputstream.getChannel();
			bytebuffer = MemoryUtil.memAlloc((int) filechannel.size() + 1);

			while (filechannel.read(bytebuffer) != -1) {
			}
		} else {
			bytebuffer = MemoryUtil.memAlloc(8192);
			ReadableByteChannel readablebytechannel = Channels.newChannel(is);

			while (readablebytechannel.read(bytebuffer) != -1) {
				if (bytebuffer.remaining() == 0) {
					bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
				}
			}
		}

		return bytebuffer;
	}
}
