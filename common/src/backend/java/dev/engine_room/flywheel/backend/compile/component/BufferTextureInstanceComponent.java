package dev.engine_room.flywheel.backend.compile.component;

import java.util.ArrayList;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.backend.glsl.generate.FnSignature;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBlock;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBuilder;
import dev.engine_room.flywheel.backend.glsl.generate.GlslExpr;
import dev.engine_room.flywheel.backend.glsl.generate.GlslStmt;
import dev.engine_room.flywheel.lib.math.MoreMath;

public class BufferTextureInstanceComponent extends InstanceAssemblerComponent {
	private static final String[] SWIZZLE_SELECTORS = { "x", "y", "z", "w" };

	public BufferTextureInstanceComponent(InstanceType<?> type) {
		super(type);
	}

	@Override
	public String name() {
		return Flywheel.rl("buffer_texture_instance_assembler").toString();
	}

	@Override
	protected void generateUnpacking(GlslBuilder builder) {
		var fnBody = new GlslBlock();

		int texels = MoreMath.ceilingDiv(layout.byteSize(), 16);

		fnBody.add(GlslStmt.raw("int base = " + UNPACK_ARG + " * " + texels + ";"));

		for (int i = 0; i < texels; i++) {
			// Fetch all the texels for the given instance ahead of time to simplify the unpacking generators.
			fnBody.add(GlslStmt.raw("uvec4 u" + i + " = texelFetch(_flw_instances, base + " + i + ");"));
		}

		var unpackArgs = new ArrayList<GlslExpr>();
		for (Layout.Element element : layout.elements()) {
			unpackArgs.add(unpackElement(element));
		}

		fnBody.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

		builder._addRaw("uniform usamplerBuffer _flw_instances;");
		builder.blankLine();
		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg("int", UNPACK_ARG)
						.build())
				.body(fnBody);
	}

	@Override
	protected GlslExpr access(int uintOffset) {
		return GlslExpr.variable("u" + (uintOffset >> 2))
				.swizzle(SWIZZLE_SELECTORS[uintOffset & 3]);
	}
}
