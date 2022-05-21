package com.jozufozu.flywheel.core.material;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class MaterialShaders {
	public static final BiConsumer<ErrorReporter, SourceFile> VERTEX_CHECK = SourceChecks.checkFunctionArity("flw_materialVertex", 0);
	public static final BiConsumer<ErrorReporter, SourceFile> FRAGMENT_CHECK = SourceChecks.checkFunctionArity("flw_materialFragment", 0);

	public static final FileResolution DEFAULT_VERTEX = createVertex(ResourceUtil.subPath(Names.DEFAULT, ".vert"));
	public static final FileResolution DEFAULT_FRAGMENT = createFragment(ResourceUtil.subPath(Names.DEFAULT, ".frag"));
	public static final FileResolution SHADED_VERTEX = createVertex(ResourceUtil.subPath(Names.SHADED, ".vert"));

	public static FileResolution createVertex(ResourceLocation location) {
		return FileResolution.get(location).validateWith(VERTEX_CHECK);
	}

	public static FileResolution createFragment(ResourceLocation location) {
		return FileResolution.get(location).validateWith(FRAGMENT_CHECK);
	}

	public static void init() {
	}

	public static class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
	}
}
