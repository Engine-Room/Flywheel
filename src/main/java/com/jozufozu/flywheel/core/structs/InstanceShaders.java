package com.jozufozu.flywheel.core.structs;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class InstanceShaders {
	public static final BiConsumer<ErrorReporter, SourceFile> CHECK = SourceChecks.checkFunctionParameterTypeExists("flw_instanceVertex", 1, 0);

	public static final FileResolution TRANSFORMED = create(ResourceUtil.subPath(Names.TRANSFORMED, ".vert"));
	public static final FileResolution ORIENTED = create(ResourceUtil.subPath(Names.ORIENTED, ".vert"));

	public static FileResolution create(ResourceLocation location) {
		return FileResolution.get(location).validateWith(CHECK);
	}

	public static void init() {
	}

	public static class Names {
		public static final ResourceLocation TRANSFORMED = Flywheel.rl("instance/transformed");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");
	}
}
