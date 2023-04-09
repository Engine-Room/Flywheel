package com.jozufozu.flywheel.lib.instance;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class InstanceTypes {
	public static final InstanceType<TransformedInstance> TRANSFORMED = InstanceType.REGISTRY.registerAndGet(new TransformedType());
	public static final InstanceType<OrientedInstance> ORIENTED = InstanceType.REGISTRY.registerAndGet(new OrientedType());

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation TRANSFORMED = ResourceUtil.subPath(Names.TRANSFORMED, ".vert");
		public static final ResourceLocation ORIENTED = ResourceUtil.subPath(Names.ORIENTED, ".vert");
	}

	public static final class Names {
		public static final ResourceLocation TRANSFORMED = Flywheel.rl("instance/transformed");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");
	}
}
