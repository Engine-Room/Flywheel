package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.model.Model;

public record InstancerKey<D extends InstancedPart>(StructType<D> type, Model model) {
}
