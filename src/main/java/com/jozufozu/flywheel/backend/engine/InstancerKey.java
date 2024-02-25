package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.context.Context;

public record InstancerKey<I extends Instance>(InstanceType<I> type, Context context, Model model, RenderStage stage) {
}
