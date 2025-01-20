package dev.engine_room.vanillin;

import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.FloatRepr;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.lib.instance.SimpleInstanceType;
import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;

public class VanillinInstanceTypes {
	public static final InstanceType<GlyphInstance> GLYPH = SimpleInstanceType.builder(GlyphInstance::new)
			.layout(LayoutBuilder.create()
					.matrix("pose", FloatRepr.FLOAT, 4)
					.vector("u0u1v0v1", FloatRepr.NORMALIZED_UNSIGNED_SHORT, 4)
					.vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4)
					.vector("light", FloatRepr.UNSIGNED_SHORT, 2)
					.build())
			.writer((ptr, instance) -> {
				ExtraMemoryOps.putMatrix4f(ptr, instance.pose);
				ExtraMemoryOps.put2x16(ptr + 64, instance.packedUs);
				ExtraMemoryOps.put2x16(ptr + 68, instance.packedVs);
				MemoryUtil.memPutByte(ptr + 72, instance.red);
				MemoryUtil.memPutByte(ptr + 73, instance.green);
				MemoryUtil.memPutByte(ptr + 74, instance.blue);
				MemoryUtil.memPutByte(ptr + 75, instance.alpha);
				ExtraMemoryOps.put2x16(ptr + 76, instance.light);
			})
			.vertexShader(Vanillin.rl("instance/glyph.vert"))
			.cullShader(Vanillin.rl("instance/cull/glyph.glsl"))
			.build();
}
