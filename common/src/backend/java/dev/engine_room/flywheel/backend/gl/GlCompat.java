package dev.engine_room.flywheel.backend.gl;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL31C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRShaderSubgroup;
import org.lwjgl.system.MemoryStack;

import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.lib.math.MoreMath;

public final class GlCompat {
	@UnknownNullability
	public static final GLCapabilities CAPABILITIES;
	static {
		GLCapabilities caps;
		try {
			caps = GL.getCapabilities();
		} catch (IllegalStateException e) {
			// This happens with vulkanmod installed.
			FlwBackend.LOGGER.warn("Failed to get GL capabilities; default Flywheel backends will be disabled.");
			caps = null;
		}
		CAPABILITIES = caps;
	}

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
		if (CAPABILITIES == null) {
			return Driver.UNKNOWN;
		}

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
		if (CAPABILITIES == null) {
			return 32;
		}
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

	private static boolean isInstancingSupported() {
		if (CAPABILITIES == null) {
			return false;
		}
		if (CAPABILITIES.OpenGL33) {
			return true;
		}
		return CAPABILITIES.GL_ARB_shader_bit_encoding;
	}

	private static boolean isIndirectSupported() {
		if (CAPABILITIES == null) {
			return false;
		}
		if (CAPABILITIES.OpenGL46) {
			return true;
		}
		return CAPABILITIES.GL_ARB_compute_shader
				&& CAPABILITIES.GL_ARB_direct_state_access
				&& CAPABILITIES.GL_ARB_gpu_shader5
				&& CAPABILITIES.GL_ARB_multi_bind
				&& CAPABILITIES.GL_ARB_multi_draw_indirect
				&& CAPABILITIES.GL_ARB_shader_draw_parameters
				&& CAPABILITIES.GL_ARB_shader_storage_buffer_object
				&& CAPABILITIES.GL_ARB_shading_language_420pack
				&& CAPABILITIES.GL_ARB_vertex_attrib_binding;
	}

	/**
	 * Try to compile a shader with progressively lower glsl versions.
	 * The first version to compile successfully is returned.
	 * @return The highest glsl version that could be compiled.
	 */
	private static GlslVersion maxGlslVersion() {
		if (CAPABILITIES == null) {
			return GlslVersion.V150;
		}

		var glslVersions = GlslVersion.values();
		// No need to test glsl 150 as that is guaranteed to be supported by MC.
		for (int i = glslVersions.length - 1; i > 0; i--) {
			var version = glslVersions[i];

			if (canCompileVersion(version)) {
				return version;
			}
		}

		return GlslVersion.V150;
	}

	private static boolean canCompileVersion(GlslVersion version) {
		int handle = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);

		// Compile the simplest possible shader.
		var source = """
				#version %d
				void main() {}
				""".formatted(version.version);

		safeShaderSource(handle, source);
		GL20.glCompileShader(handle);

		boolean success = Compilation.compiledSuccessfully(handle);

		GL20.glDeleteShader(handle);

		return success;
	}
}
