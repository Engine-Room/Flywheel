package dev.engine_room.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.Collections;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.layout.Layout;
import dev.engine_room.flywheel.backend.compile.LayoutInterpreter;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.glsl.generate.GlslBuilder;

public class InstanceStructComponent implements SourceComponent {
	private static final String STRUCT_NAME = "FlwInstance";

	private final Layout layout;

	public InstanceStructComponent(InstanceType<?> type) {
		layout = type.layout();
	}

	@Override
	public String name() {
		return Flywheel.rl("instance_struct").toString();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return Collections.emptyList();
	}

	@Override
	public String source() {
		var builder = new GlslBuilder();

		var instance = builder.struct();
		instance.setName(STRUCT_NAME);
		for (var element : layout.elements()) {
			instance.addField(LayoutInterpreter.typeName(element.type()), element.name());
		}

		builder.blankLine();
		return builder.build();
	}
}
