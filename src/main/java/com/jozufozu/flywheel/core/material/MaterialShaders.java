package com.jozufozu.flywheel.core.material;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class MaterialShaders {
	public static FileResolution DEFAULT_VERTEX;
	public static FileResolution DEFAULT_FRAGMENT;
	public static FileResolution SHADED_VERTEX;

	public static void init() {
		var checkVert = SourceChecks.checkFunctionArity("flw_materialVertex", 0);
		var checkFrag = SourceChecks.checkFunctionArity("flw_materialFragment", 0);

		DEFAULT_VERTEX = FileResolution.get(ResourceUtil.subPath(Names.DEFAULT, ".vert"))
				.validateWith(checkVert);
		DEFAULT_FRAGMENT = FileResolution.get(ResourceUtil.subPath(Names.DEFAULT, ".frag"))
				.validateWith(checkFrag);
		SHADED_VERTEX = FileResolution.get(ResourceUtil.subPath(Names.SHADED, ".vert"))
				.validateWith(checkVert);
	}

	public static class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
	}
}
