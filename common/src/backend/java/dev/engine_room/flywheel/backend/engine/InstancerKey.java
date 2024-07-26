package dev.engine_room.flywheel.backend.engine;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.engine.embed.Environment;

public record InstancerKey<I extends Instance>(Environment environment, InstanceType<I> type, Model model,
											   VisualType visualType) {
}
