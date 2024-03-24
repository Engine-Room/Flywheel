package com.jozufozu.flywheel.backend.compile.component;

import java.util.ArrayList;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectBuffers;
import com.jozufozu.flywheel.backend.glsl.generate.FnSignature;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBlock;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;
import com.jozufozu.flywheel.backend.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.backend.glsl.generate.GlslStmt;

public class SsboInstanceComponent extends InstanceAssemblerComponent {
	public SsboInstanceComponent(InstanceType<?> type) {
		super(type);
	}

	@Override
	public String name() {
		return Flywheel.rl("ssbo_instance_assembler").toString();
	}

	@Override
	protected void generateUnpacking(GlslBuilder builder) {
		var fnBody = new GlslBlock();

		var uintCount = layout.byteSize() / 4;

		fnBody.add(GlslStmt.raw("uint base = " + UNPACK_ARG + " * " + uintCount + "u;"));

		for (int i = 0; i < uintCount; i++) {
			// Retrieve all the uints for the given instance ahead of time to simplify the unpacking generators.
			fnBody.add(GlslStmt.raw("uint u" + i + " = _flw_instances[base + " + i + "u];"));
		}

		var unpackArgs = new ArrayList<GlslExpr>();
		int uintOffset = 0;
		for (Layout.Element element : layout.elements()) {
			unpackArgs.add(unpackElement(element, uintOffset));
			// Element byte size is always a multiple of 4
			uintOffset += element.type().byteSize() / 4;
		}

		fnBody.ret(GlslExpr.call(STRUCT_NAME, unpackArgs));

		builder._addRaw("layout(std430, binding = " + IndirectBuffers.INSTANCE_INDEX + ") restrict readonly buffer InstanceBuffer {\n"
				+ "    uint _flw_instances[];\n"
				+ "};");
		builder.blankLine();
		builder.function()
				.signature(FnSignature.create()
						.returnType(STRUCT_NAME)
						.name(UNPACK_FN_NAME)
						.arg("uint", UNPACK_ARG)
						.build())
				.body(fnBody);
	}

	@Override
	protected GlslExpr access(int uintOffset) {
		return GlslExpr.variable("u" + uintOffset);
	}
}
