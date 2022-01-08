package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.GameStateRegistry;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.Resolver;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingProgram;
import com.jozufozu.flywheel.core.shader.NormalDebugStateProvider;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Contexts {

	public static ProgramCompiler<WorldProgram> WORLD;
	public static ProgramCompiler<CrumblingProgram> CRUMBLING;

	public static void flwInit(GatherContextEvent event) {
		GameStateRegistry.register(NormalDebugStateProvider.INSTANCE);

		FileResolution worldBuiltins = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.WORLD, ".glsl"));
		FileResolution crumblingBuiltins = Resolver.INSTANCE.get(ResourceUtil.subPath(Names.CRUMBLING, ".glsl"));

		WORLD = Templates.INSTANCING.programCompiler(WorldProgram::new, worldBuiltins);
		CRUMBLING = Templates.INSTANCING.programCompiler(CrumblingProgram::new, crumblingBuiltins);
	}

	public static class Names {
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
	}
}
