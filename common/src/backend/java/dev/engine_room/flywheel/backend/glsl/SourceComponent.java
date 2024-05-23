package dev.engine_room.flywheel.backend.glsl;

import java.util.Collection;

public interface SourceComponent {
	Collection<? extends SourceComponent> included();

	String source();

	String name();
}
