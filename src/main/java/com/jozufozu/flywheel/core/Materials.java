package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.event.GatherContextEvent;

import net.minecraft.util.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class Materials {
	public static final MaterialSpec<OrientedData> ORIENTED = new MaterialSpec<>(Locations.ORIENTED, Programs.ORIENTED, Formats.UNLIT_MODEL, Formats.ORIENTED, OrientedData::new);
	public static final MaterialSpec<ModelData> TRANSFORMED = new MaterialSpec<>(Locations.MODEL, Programs.TRANSFORMED, Formats.UNLIT_MODEL, Formats.TRANSFORMED, ModelData::new);

	public static void flwInit(GatherContextEvent event) {
		event.getBackend()
				.register(ORIENTED);
		event.getBackend()
				.register(TRANSFORMED);
	}

	public static class Locations {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
	}
}
