package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.InstancePart;
import com.jozufozu.flywheel.api.struct.StructType;

public record InstancerKey<P extends InstancePart>(StructType<P> type, Model model, RenderStage stage) {
}
