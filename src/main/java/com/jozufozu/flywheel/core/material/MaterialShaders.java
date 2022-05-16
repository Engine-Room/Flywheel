package com.jozufozu.flywheel.core.material;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class MaterialShaders {
	public static FileResolution DEFAULT_VERTEX;
	public static FileResolution DEFAULT_FRAGMENT;
	public static FileResolution SHADED_VERTEX;

	public static void flwInit(GatherContextEvent event) {
		DEFAULT_VERTEX = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.DEFAULT, ".vert"));
		DEFAULT_FRAGMENT = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.DEFAULT, ".frag"));
		SHADED_VERTEX = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.SHADED, ".vert"));
	}

	public static class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
	}
}
