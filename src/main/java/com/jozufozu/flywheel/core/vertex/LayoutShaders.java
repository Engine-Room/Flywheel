package com.jozufozu.flywheel.core.vertex;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class LayoutShaders {
	public static final BiConsumer<ErrorReporter, SourceFile> CHECK = SourceChecks.checkFunctionArity("flw_layoutVertex", 0);

	public static final FileResolution BLOCK = create(ResourceUtil.subPath(Names.BLOCK, ".vert"));
	public static final FileResolution POS_TEX_NORMAL = create(ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert"));

	public static FileResolution create(ResourceLocation location) {
		return FileResolution.get(location).validateWith(CHECK);
	}

	public static void init() {
	}

	public static class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
