package com.jozufozu.flywheel.lib.context;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class Contexts {
	public static final SimpleContext WORLD = Context.REGISTRY.registerAndGet(new SimpleContext(Files.WORLD_VERTEX, Files.WORLD_FRAGMENT, program -> {
		program.bind();
		program.setSamplerBinding("flw_diffuseTex", 0);
		program.setSamplerBinding("flw_overlayTex", 1);
		program.setSamplerBinding("flw_lightTex", 2);
		GlProgram.unbind();
	}));

	// TODO: can we make crumbling a fragment material?
	public static final SimpleContext CRUMBLING = Context.REGISTRY.registerAndGet(new SimpleContext(Files.WORLD_VERTEX, Files.CRUMBLING_FRAGMENT, program -> {
		program.bind();
		program.setSamplerBinding("flw_diffuseTex", 0);
		GlProgram.unbind();
	}));

	private Contexts() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation WORLD_VERTEX = ResourceUtil.subPath(Names.WORLD, ".vert");
		public static final ResourceLocation WORLD_FRAGMENT = ResourceUtil.subPath(Names.WORLD, ".frag");
		public static final ResourceLocation CRUMBLING_VERTEX = ResourceUtil.subPath(Names.CRUMBLING, ".vert");
		public static final ResourceLocation CRUMBLING_FRAGMENT = ResourceUtil.subPath(Names.CRUMBLING, ".frag");
	}

	public static final class Names {
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
	}
}
