package com.jozufozu.flywheel.core.structs;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class InstanceShaders {
	public static FileResolution MODEL;
	public static FileResolution ORIENTED;

	public static void flwInit(GatherContextEvent event) {
		MODEL = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.MODEL, ".vert"));
		ORIENTED = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.ORIENTED, ".vert"));
	}

	public static class Names {
		public static final ResourceLocation MODEL = Flywheel.rl("instance/model");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");
	}
}
