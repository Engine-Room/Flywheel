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

		FileResolution worldBuiltins = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.WORLD, ".glsl"));
		FileResolution crumblingBuiltins = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.CRUMBLING, ".glsl"));

		WORLD = ProgramCompiler.create(Templates.INSTANCING, WorldProgram::new, worldBuiltins);
		CRUMBLING = ProgramCompiler.create(Templates.INSTANCING, CrumblingProgram::new, crumblingBuiltins);
	}

	public static class Names {
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
	}
}
