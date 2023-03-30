package com.jozufozu.flywheel.lib.format;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.component.ComponentRegistry;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Formats {
	public static final BlockVertex BLOCK = ComponentRegistry.register(new BlockVertex());
	public static final PosTexNormalVertex POS_TEX_NORMAL = ComponentRegistry.register(new PosTexNormalVertex());

	public static void init() {
		// noop
	}

	public static class Files {
		public static final ResourceLocation BLOCK_LAYOUT = ResourceUtil.subPath(Names.BLOCK, ".vert");
		public static final ResourceLocation POS_TEX_NORMAL_LAYOUT = ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert");
	}

	public static class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
