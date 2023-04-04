package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;

public record InstancerKey<D extends InstancedPart>(StructType<D> type, Model model, RenderStage stage) {
}
