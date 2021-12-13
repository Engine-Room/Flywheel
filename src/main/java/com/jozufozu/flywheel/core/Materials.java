package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.materials.model.ModelType;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.core.materials.oriented.OrientedType;
import com.jozufozu.flywheel.event.GatherContextEvent;

import net.minecraft.resources.ResourceLocation;

public class Materials {

	public static final StructType<OrientedData> ORIENTED = new OrientedType();
	public static final StructType<ModelData> TRANSFORMED = new ModelType();

	public static void flwInit(GatherContextEvent event) {
		Backend backend = event.getBackend();
		backend.register(Names.ORIENTED, ORIENTED);
		backend.register(Names.MODEL, TRANSFORMED);
	}

	public static class Names {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
	}
}
