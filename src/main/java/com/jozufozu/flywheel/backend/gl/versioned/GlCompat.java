package com.jozufozu.flywheel.backend.gl.versioned;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import net.minecraft.Util;

/**
 * An instance of this class stores information about what OpenGL features are available.
 * <br>
 * Each field stores an enum variant that provides access to the most appropriate version of a feature for the current
 * system.
 */
public class GlCompat {

	private static GlCompat instance;

	public static GlCompat getInstance() {
		if (instance == null) {
			instance = new GlCompat();
		}
		return instance;
	}

	public final InstancedArrays instancedArrays;
	public final BufferStorage bufferStorage;
	public final boolean amd;

	private GlCompat() {
		GLCapabilities caps = GL.createCapabilities();
		instancedArrays = getLatest(InstancedArrays.class, caps);
		bufferStorage = getLatest(BufferStorage.class, caps);

		if (Util.getPlatform() == Util.OS.WINDOWS) {
			String vendor = GL20C.glGetString(GL20C.GL_VENDOR);
			// vendor string I got was "ATI Technologies Inc."
			amd = vendor.contains("ATI") || vendor.contains("AMD");
		} else {
			amd = false;
		}
	}

	public boolean onAMDWindows() {
		return amd;
	}

    public boolean instancedArraysSupported() {
		return instancedArrays != InstancedArrays.UNSUPPORTED;
	}

	public boolean bufferStorageSupported() {
		return bufferStorage != BufferStorage.UNSUPPORTED;
	}

	/**
	 * Get the most compatible version of a specific OpenGL feature by iterating over enum constants in order.
	 *
	 * @param clazz The class of the versioning enum.
	 * @param caps  The current system's supported features.
	 * @param <V>   The type of the versioning enum.
	 * @return The first defined enum variant to return true.
	 */
	public static <V extends Enum<V> & GlVersioned> V getLatest(Class<V> clazz, GLCapabilities caps) {
		V[] constants = clazz.getEnumConstants();
		V last = constants[constants.length - 1];
		if (!last.supported(caps)) {
			throw new IllegalStateException("");
		}

		return Arrays.stream(constants)
				.filter(it -> it.supported(caps))
				.findFirst()
				.get();
	}

	/**
	 * Copied from:
	 * <br> https://github.com/grondag/canvas/commit/820bf754092ccaf8d0c169620c2ff575722d7d96
	 *
	 * <p>Identical in function to {@link GL20C#glShaderSource(int, CharSequence)} but
	 * passes a null pointer for string length to force the driver to rely on the null
	 * terminator for string length.  This is a workaround for an apparent flaw with some
	 * AMD drivers that don't receive or interpret the length correctly, resulting in
	 * an access violation when the driver tries to read past the string memory.
	 *
	 * <p>Hat tip to fewizz for the find and the fix.
	 */
	public static void safeShaderSource(int glId, CharSequence source) {
		final MemoryStack stack = MemoryStack.stackGet();
		final int stackPointer = stack.getPointer();

		try {
			final ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
			final PointerBuffer pointers = stack.mallocPointer(1);
			pointers.put(sourceBuffer);

			GL20C.nglShaderSource(glId, 1, pointers.address0(), 0);
			org.lwjgl.system.APIUtil.apiArrayFree(pointers.address0(), 1);
		} finally {
			stack.setPointer(stackPointer);
		}
	}
}

