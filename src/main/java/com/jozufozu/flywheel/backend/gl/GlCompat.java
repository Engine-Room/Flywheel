package com.jozufozu.flywheel.backend.gl;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRShaderSubgroup;
import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.lib.math.MoreMath;

public final class GlCompat {
	public static final GLCapabilities CAPABILITIES = GL.getCapabilities();
	public static final Driver DRIVER = readVendorString();
	public static final int SUBGROUP_SIZE = subgroupSize();
	public static final boolean ALLOW_DSA = true;
	public static final GlslVersion MAX_GLSL_VERSION = maxGlslVersion();

	public static final boolean SUPPORTS_INSTANCING = isInstancingSupported();
	public static final boolean SUPPORTS_INDIRECT = isIndirectSupported();

	private GlCompat() {
	}

	public static void init() {
	}

	public static int getComputeGroupCount(int invocations) {
		return MoreMath.ceilingDiv(invocations, SUBGROUP_SIZE);
	}

	/**
	 * Modified from:
	 * <br> <a href="https://github.com/grondag/canvas/commit/820bf754092ccaf8d0c169620c2ff575722d7d96">canvas</a>
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
		try (MemoryStack stack = MemoryStack.stackPush()) {
			final ByteBuffer sourceBuffer = stack.UTF8(source, true);
			final PointerBuffer pointers = stack.mallocPointer(1);
			pointers.put(sourceBuffer);
			GL20C.nglShaderSource(glId, 1, pointers.address0(), 0);
		}
	}

	private static Driver readVendorString() {
		String vendor = GL20C.glGetString(GL20C.GL_VENDOR);

		if (vendor == null) {
			return Driver.UNKNOWN;
		}

		// vendor string I got was "ATI Technologies Inc."
		if (vendor.contains("ATI") || vendor.contains("AMD")) {
			return Driver.AMD;
		} else if (vendor.contains("NVIDIA")) {
			return Driver.NVIDIA;
		} else if (vendor.contains("Intel")) {
			return Driver.INTEL;
		} else if (vendor.contains("Mesa")) {
			return Driver.MESA;
		}

		return Driver.UNKNOWN;
	}

	private static int subgroupSize() {
		if (CAPABILITIES.GL_KHR_shader_subgroup) {
			return GL31C.glGetInteger(KHRShaderSubgroup.GL_SUBGROUP_SIZE_KHR);
		}

		// Try to guess.
		// Newer (RDNA) AMD cards have 32 threads in a wavefront, older ones have 64.
		// I assume the newer drivers will implement the above extension, so 64 is a
		// reasonable guess for AMD hardware. In the worst case we'll just spread
		// load across multiple SIMDs
		return DRIVER == Driver.AMD || DRIVER == Driver.MESA ? 64 : 32;
	}

	private static GlslVersion maxGlslVersion() {
		if (CAPABILITIES.OpenGL46) {
			return GlslVersion.V460;
		} else if (CAPABILITIES.OpenGL45) {
			return GlslVersion.V450;
		} else if (CAPABILITIES.OpenGL44) {
			return GlslVersion.V440;
		} else if (CAPABILITIES.OpenGL43) {
			return GlslVersion.V430;
		} else if (CAPABILITIES.OpenGL42) {
			return GlslVersion.V420;
		} else if (CAPABILITIES.OpenGL41) {
			return GlslVersion.V410;
		} else if (CAPABILITIES.OpenGL40) {
			return GlslVersion.V400;
		} else if (CAPABILITIES.OpenGL33) {
			return GlslVersion.V330;
		} else {
			return GlslVersion.V150;
		}
	}

	private static boolean isInstancingSupported() {
		if (!CAPABILITIES.OpenGL33) {
			return false;
		}
		return true;
	}

	private static boolean isIndirectSupported() {
		// The GL requirement cannot be lower because GL_ARB_compute_shader requires at least GL 4.2.
		if (!CAPABILITIES.OpenGL42) {
			return false;
		}
		if (CAPABILITIES.OpenGL46) {
			return true;
		}
		return CAPABILITIES.GL_ARB_compute_shader
				&& CAPABILITIES.GL_ARB_direct_state_access
				&& CAPABILITIES.GL_ARB_multi_bind
				&& CAPABILITIES.GL_ARB_multi_draw_indirect
				&& CAPABILITIES.GL_ARB_shader_draw_parameters
				&& CAPABILITIES.GL_ARB_shader_storage_buffer_object;
	}
}
