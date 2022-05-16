package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.core.shader.NormalDebugStateProvider;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Contexts {

	public static ProgramCompiler<WorldProgram> WORLD;
	public static ProgramCompiler<CrumblingProgram> CRUMBLING;

	public static void flwInit(GatherContextEvent event) {
		GameStateRegistry.register(NormalDebugStateProvider.INSTANCE);

		FileResolution worldVertex = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.WORLD, ".vert"));
		FileResolution worldFragment = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.WORLD, ".frag"));
		FileResolution crumblingVertex = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.CRUMBLING, ".vert"));
		FileResolution crumblingFragment = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.CRUMBLING, ".frag"));

		WORLD = ProgramCompiler.create(WorldProgram::new, worldVertex, worldFragment);
		CRUMBLING = ProgramCompiler.create(CrumblingProgram::new, crumblingVertex, crumblingFragment);
	}

	public static class Names {
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
	}
}
