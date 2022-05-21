package com.jozufozu.flywheel.core;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Contexts {
	public static final BiConsumer<ErrorReporter, SourceFile> VERTEX_CHECK = SourceChecks.checkFunctionArity("flw_contextVertex", 0);
	public static final BiConsumer<ErrorReporter, SourceFile> FRAGMENT_CHECK = SourceChecks.checkFunctionArity("flw_contextFragment", 0);

	public static final ProgramCompiler<WorldProgram> WORLD;
	public static final ProgramCompiler<CrumblingProgram> CRUMBLING;

	static {
		var worldVertex = createVertex(ResourceUtil.subPath(Names.WORLD, ".vert"));
		var worldFragment = createFragment(ResourceUtil.subPath(Names.WORLD, ".frag"));
		var crumblingVertex = createVertex(ResourceUtil.subPath(Names.CRUMBLING, ".vert"));
		var crumblingFragment = createFragment(ResourceUtil.subPath(Names.CRUMBLING, ".frag"));

		WORLD = ProgramCompiler.create(WorldProgram::new, worldVertex, worldFragment, GLSLVersion.V330);
		CRUMBLING = ProgramCompiler.create(CrumblingProgram::new, crumblingVertex, crumblingFragment, GLSLVersion.V330);
	}

	public static FileResolution createVertex(ResourceLocation location) {
		return FileResolution.get(location).validateWith(VERTEX_CHECK);
	}

	public static FileResolution createFragment(ResourceLocation location) {
		return FileResolution.get(location).validateWith(FRAGMENT_CHECK);
	}

	public static void init() {	
	}

	public static class Names {
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
	}
}
