package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Model;

public record InstancerKey<I extends Instance>(Environment environment, InstanceType<I> type, Model model,
											   RenderStage stage) {
}
