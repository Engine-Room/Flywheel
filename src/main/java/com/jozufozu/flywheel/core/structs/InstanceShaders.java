package com.jozufozu.flywheel.core.structs;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class InstanceShaders {
	public static FileResolution MODEL;
	public static FileResolution ORIENTED;

	public static void init() {
		var check = SourceChecks.checkFunctionParameterTypeExists("flw_instanceVertex", 1, 0);

		MODEL = FileResolution.get(ResourceUtil.subPath(Names.MODEL, ".vert"))
				.validateWith(check);
		ORIENTED = FileResolution.get(ResourceUtil.subPath(Names.ORIENTED, ".vert"))
				.validateWith(check);
	}

	public static class Names {
		public static final ResourceLocation MODEL = Flywheel.rl("instance/model");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");
	}
}
