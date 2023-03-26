package com.jozufozu.flywheel.core;

import java.util.function.BiConsumer;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.source.SourceChecks;
import com.jozufozu.flywheel.core.source.SourceFile;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.jozufozu.flywheel.core.uniform.FlwUniformProvider;
import com.jozufozu.flywheel.core.vertex.Formats;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public class Components {


	public static final FlwUniformProvider UNIFORM_PROVIDER = ComponentRegistry.register(new FlwUniformProvider());
	public static final ContextShader WORLD = ComponentRegistry.register(new ContextShader(Files.WORLD_VERTEX, Files.WORLD_FRAGMENT));
	public static final ContextShader CRUMBLING = ComponentRegistry.register(new ContextShader(Files.WORLD_VERTEX, Files.CRUMBLING_FRAGMENT));

	public static void init() {
		Files.init();
		Formats.init();
		StructTypes.init();
		Materials.init();
		Pipelines.init();
	}

	public static class Files {

        public static final FileResolution UNIFORMS = uniform(Flywheel.rl("uniform/flywheel.glsl"));
        public static final FileResolution BLOCK_LAYOUT = layoutVertex(ResourceUtil.subPath(Names.BLOCK, ".vert"));
		public static final FileResolution POS_TEX_NORMAL_LAYOUT = layoutVertex(ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert"));
		public static final FileResolution TRANSFORMED = instanceVertex(ResourceUtil.subPath(Names.TRANSFORMED, ".vert"));
		public static final FileResolution ORIENTED = instanceVertex(ResourceUtil.subPath(Names.ORIENTED, ".vert"));
		public static final FileResolution DEFAULT_VERTEX = materialVertex(ResourceUtil.subPath(Names.DEFAULT, ".vert"));
		public static final FileResolution SHADED_VERTEX = materialVertex(ResourceUtil.subPath(Names.SHADED, ".vert"));
		public static final FileResolution DEFAULT_FRAGMENT = materialFragment(ResourceUtil.subPath(Names.DEFAULT, ".frag"));
		public static final FileResolution CUTOUT_FRAGMENT = materialFragment(ResourceUtil.subPath(Names.CUTOUT, ".frag"));
		public static final FileResolution WORLD_VERTEX = contextVertex(ResourceUtil.subPath(Names.WORLD, ".vert"));
		public static final FileResolution WORLD_FRAGMENT = contextFragment(ResourceUtil.subPath(Names.WORLD, ".frag"));
		public static final FileResolution CRUMBLING_VERTEX = contextVertex(ResourceUtil.subPath(Names.CRUMBLING, ".vert"));
		public static final FileResolution CRUMBLING_FRAGMENT = contextFragment(ResourceUtil.subPath(Names.CRUMBLING, ".frag"));

		private static FileResolution compute(ResourceLocation rl) {
			return FileResolution.get(rl);
		}

		private static FileResolution uniform(ResourceLocation location) {
			return FileResolution.get(location);
		}

		private static FileResolution layoutVertex(ResourceLocation location) {
			return FileResolution.get(location)
					.validateWith(Checks.LAYOUT_VERTEX);
		}

		private static FileResolution instanceVertex(ResourceLocation location) {
			return FileResolution.get(location); // .validateWith(Checks.INSTANCE_VERTEX);
		}

		private static FileResolution materialVertex(ResourceLocation location) {
			return FileResolution.get(location)
					.validateWith(Checks.MATERIAL_VERTEX);
		}

		private static FileResolution materialFragment(ResourceLocation location) {
			return FileResolution.get(location)
					.validateWith(Checks.MATERIAL_FRAGMENT);
		}

		private static FileResolution contextVertex(ResourceLocation location) {
			return FileResolution.get(location)
					.validateWith(Checks.CONTEXT_VERTEX);
		}

		private static FileResolution contextFragment(ResourceLocation location) {
			return FileResolution.get(location)
					.validateWith(Checks.CONTEXT_FRAGMENT);
		}

		public static void init() {
			// noop, just in case
		}
	}

	public static class Checks {

		public static final BiConsumer<ErrorReporter, SourceFile> LAYOUT_VERTEX = SourceChecks.checkFunctionArity("flw_layoutVertex", 0);
		public static final BiConsumer<ErrorReporter, SourceFile> INSTANCE_VERTEX = SourceChecks.checkFunctionParameterTypeExists("flw_instanceVertex", 1, 0);
		public static final BiConsumer<ErrorReporter, SourceFile> MATERIAL_VERTEX = SourceChecks.checkFunctionArity("flw_materialVertex", 0);
		public static final BiConsumer<ErrorReporter, SourceFile> MATERIAL_FRAGMENT = SourceChecks.checkFunctionArity("flw_materialFragment", 0);
		public static final BiConsumer<ErrorReporter, SourceFile> CONTEXT_VERTEX = SourceChecks.checkFunctionArity("flw_contextVertex", 0);
		public static final BiConsumer<ErrorReporter, SourceFile> CONTEXT_FRAGMENT = SourceChecks.checkFunctionArity("flw_contextFragment", 0)
				.andThen(SourceChecks.checkFunctionArity("flw_initFragment", 0));

		public static final BiConsumer<ErrorReporter, SourceFile> PIPELINE = SourceChecks.checkFunctionArity("main", 0);
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
