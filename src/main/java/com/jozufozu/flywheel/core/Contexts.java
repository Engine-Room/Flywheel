package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.core.shader.NormalDebugStateProvider;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Contexts {

	public static ProgramCompiler<WorldProgram> WORLD;
	public static ProgramCompiler<CrumblingProgram> CRUMBLING;

	public static void init() {
		GameStateRegistry.register(NormalDebugStateProvider.INSTANCE);

		var checkFrag = SourceChecks.checkFunctionArity("flw_contextFragment", 0);
		var checkVert = SourceChecks.checkFunctionArity("flw_contextVertex", 0);

		var worldVertex = FileResolution.get(ResourceUtil.subPath(Names.WORLD, ".vert"))
				.validateWith(checkVert);
		var worldFragment = FileResolution.get(ResourceUtil.subPath(Names.WORLD, ".frag"))
				.validateWith(checkFrag);
		var crumblingVertex = FileResolution.get(ResourceUtil.subPath(Names.CRUMBLING, ".vert"))
				.validateWith(checkVert);
		var crumblingFragment = FileResolution.get(ResourceUtil.subPath(Names.CRUMBLING, ".frag"))
				.validateWith(checkFrag);

		WORLD = ProgramCompiler.create(WorldProgram::new, worldVertex, worldFragment, GLSLVersion.V330);
		CRUMBLING = ProgramCompiler.create(CrumblingProgram::new, crumblingVertex, crumblingFragment, GLSLVersion.V330);
	}

	public static class Names {
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
	}
}
