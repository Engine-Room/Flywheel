package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.core.context.SimpleContext;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.uniform.FlwShaderUniforms;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Components {


	public static final FlwShaderUniforms UNIFORM_PROVIDER = ComponentRegistry.register(new FlwShaderUniforms());
	public static final SimpleContext WORLD = ComponentRegistry.register(new SimpleContext(Files.WORLD_VERTEX, Files.WORLD_FRAGMENT));
	public static final SimpleContext CRUMBLING = ComponentRegistry.register(new SimpleContext(Files.WORLD_VERTEX, Files.CRUMBLING_FRAGMENT));

	public static void init() {
		Formats.init();
		StructTypes.init();
		Materials.init();
		Pipelines.init();
	}

	public static class Files {

		public static final ResourceLocation UNIFORMS = Flywheel.rl("uniform/flywheel.glsl");
		public static final ResourceLocation BLOCK_LAYOUT = ResourceUtil.subPath(Names.BLOCK, ".vert");
		public static final ResourceLocation POS_TEX_NORMAL_LAYOUT = ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert");
		public static final ResourceLocation TRANSFORMED = ResourceUtil.subPath(Names.TRANSFORMED, ".vert");
		public static final ResourceLocation ORIENTED = ResourceUtil.subPath(Names.ORIENTED, ".vert");
		public static final ResourceLocation DEFAULT_VERTEX = ResourceUtil.subPath(Names.DEFAULT, ".vert");
		public static final ResourceLocation SHADED_VERTEX = ResourceUtil.subPath(Names.SHADED, ".vert");
		public static final ResourceLocation DEFAULT_FRAGMENT = ResourceUtil.subPath(Names.DEFAULT, ".frag");
		public static final ResourceLocation CUTOUT_FRAGMENT = ResourceUtil.subPath(Names.CUTOUT, ".frag");
		public static final ResourceLocation WORLD_VERTEX = ResourceUtil.subPath(Names.WORLD, ".vert");
		public static final ResourceLocation WORLD_FRAGMENT = ResourceUtil.subPath(Names.WORLD, ".frag");
		public static final ResourceLocation CRUMBLING_VERTEX = ResourceUtil.subPath(Names.CRUMBLING, ".vert");
		public static final ResourceLocation CRUMBLING_FRAGMENT = ResourceUtil.subPath(Names.CRUMBLING, ".frag");
	}

	public static class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");

		public static final ResourceLocation TRANSFORMED = Flywheel.rl("instance/transformed");
		public static final ResourceLocation ORIENTED = Flywheel.rl("instance/oriented");

		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation CUTOUT = Flywheel.rl("material/cutout");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
		public static final ResourceLocation WORLD = Flywheel.rl("context/world");
		public static final ResourceLocation CRUMBLING = Flywheel.rl("context/crumbling");
		public static final ResourceLocation DRAW_INDIRECT = Flywheel.rl("compute/draw_instances");
	}
}
