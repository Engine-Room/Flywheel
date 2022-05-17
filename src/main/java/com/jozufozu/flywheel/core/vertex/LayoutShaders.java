package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class LayoutShaders {
	public static FileResolution BLOCK;
	public static FileResolution POS_TEX_NORMAL;

	public static void init() {
		var check = SourceChecks.checkFunctionArity("flw_layoutVertex", 0);

		BLOCK = FileResolution.get(ResourceUtil.subPath(Names.BLOCK, ".vert"))
				.validateWith(check);

		POS_TEX_NORMAL = FileResolution.get(ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert"))
				.validateWith(check);
	}

	public static class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
