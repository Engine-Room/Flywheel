package com.jozufozu.flywheel.backend.engine;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;

public record InstancerKey<I extends Instance>(@Nullable VisualEmbedding embedding, InstanceType<I> type, Model model,
											   RenderStage stage) {
}
