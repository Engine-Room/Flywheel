package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class LayoutShaders {
	public static FileResolution BLOCK;
	public static FileResolution POS_TEX_NORMAL;

	public static void flwInit(GatherContextEvent event) {
		BLOCK = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.BLOCK, ".vert"));
		POS_TEX_NORMAL = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert"));
	}

	public static class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
