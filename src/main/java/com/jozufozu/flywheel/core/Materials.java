package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.api.MaterialSpec;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.materials.model.ModelType;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.core.materials.oriented.OrientedType;
import com.jozufozu.flywheel.event.GatherContextEvent;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Materials {
	public static final StructType<OrientedData> ORIENTED_TYPE = new OrientedType();
	public static final StructType<ModelData> TRANSFORMED_TYPE = new ModelType();

	public static final MaterialSpec<OrientedData> ORIENTED = new MaterialSpec<>(Names.ORIENTED, Programs.ORIENTED, ORIENTED_TYPE);
	public static final MaterialSpec<ModelData> TRANSFORMED = new MaterialSpec<>(Names.MODEL, Programs.TRANSFORMED, TRANSFORMED_TYPE);

	public static void flwInit(GatherContextEvent event) {
		event.getBackend()
				.register(ORIENTED);
		event.getBackend()
				.register(TRANSFORMED);
	}

	public static class Names {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
	}
}
