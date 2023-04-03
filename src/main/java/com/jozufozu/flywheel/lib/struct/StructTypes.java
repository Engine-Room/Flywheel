package com.jozufozu.flywheel.lib.struct;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class StructTypes {
	public static final StructType<TransformedPart> TRANSFORMED = StructType.REGISTRY.registerAndGet(new TransformedType());
	public static final StructType<OrientedPart> ORIENTED = StructType.REGISTRY.registerAndGet(new OrientedType());

	public static void init() {
		// noop
	}

	public static class Files {
		public static final ResourceLocation TRANSFORMED = ResourceUtil.subPath(Names.TRANSFORMED, ".vert");
		public static final ResourceLocation ORIENTED = ResourceUtil.subPath(Names.ORIENTED, ".vert");
	}

	public static class Names {
		public static final ResourceLocation TRANSFORMED = Flywheel.rl("instance/transformed");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");
	}
}
