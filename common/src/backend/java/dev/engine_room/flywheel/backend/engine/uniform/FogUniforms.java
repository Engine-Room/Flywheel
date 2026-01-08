package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Vector4fc;

public final class FogUniforms extends UniformWriter {
	private static final int SIZE = 4 * 10;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FOG_INDEX, SIZE);

	public static void update(Vector4fc color, float environmentalStart, float environmentalEnd,
							  float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd) {
		long ptr = BUFFER.ptr();

		ptr = writeVec4(ptr, color);
		ptr = writeVec2(ptr, environmentalStart, environmentalEnd);
		ptr = writeVec2(ptr, renderDistanceStart, renderDistanceEnd);
		ptr = writeFloat(ptr, skyEnd);
		ptr = writeFloat(ptr, cloudEnd);

		BUFFER.markDirty();
	}
}
