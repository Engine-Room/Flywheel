package com.jozufozu.flywheel.backend.compile.component;

import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Collections;

import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.backend.compile.LayoutInterpreter;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;
import com.jozufozu.flywheel.backend.glsl.generate.GlslBuilder;

public abstract class InstanceAssemblerComponent implements SourceComponent {
	protected static final String STRUCT_NAME = "FlwInstance";
	protected static final String UNPACK_FN_NAME = "_flw_unpackInstance";

	protected static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

	protected final Layout layout;

	public InstanceAssemblerComponent(InstanceType<?> type) {
		layout = type.layout();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();
		generateInstanceStruct(builder);
		builder.blankLine();
		generateUnpacking(builder);
		builder.blankLine();
		return builder.build();
	}

	protected void generateInstanceStruct(GlslBuilder builder) {
		var instance = builder.struct();
		instance.setName(STRUCT_NAME);
		for (var element : layout.elements()) {
			instance.addField(LayoutInterpreter.typeName(element.type()), element.name());
		}
	}

	protected abstract void generateUnpacking(GlslBuilder builder);
}
