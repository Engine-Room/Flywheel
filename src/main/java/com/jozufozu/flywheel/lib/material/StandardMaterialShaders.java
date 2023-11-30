package com.jozufozu.flywheel.lib.material;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.material.MaterialShaders;

import net.minecraft.resources.ResourceLocation;

public final class StandardMaterialShaders {
	public static final MaterialShaders DEFAULT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Files.DEFAULT_VERTEX, Files.DEFAULT_FRAGMENT));
	public static final MaterialShaders SHADED = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Files.SHADED_VERTEX, Files.DEFAULT_FRAGMENT));
	public static final MaterialShaders CUTOUT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Files.DEFAULT_VERTEX, Files.CUTOUT_FRAGMENT));
	public static final MaterialShaders SHADED_CUTOUT = MaterialShaders.REGISTRY.registerAndGet(new SimpleMaterialShaders(Files.SHADED_VERTEX, Files.CUTOUT_FRAGMENT));

	private StandardMaterialShaders() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation DEFAULT_VERTEX = Names.DEFAULT.withSuffix(".vert");
		public static final ResourceLocation SHADED_VERTEX = Names.SHADED.withSuffix(".vert");
		public static final ResourceLocation DEFAULT_FRAGMENT = Names.DEFAULT.withSuffix(".frag");
		public static final ResourceLocation CUTOUT_FRAGMENT = Names.CUTOUT.withSuffix(".frag");
	}

	public static final class Names {
		public static final ResourceLocation DEFAULT = Flywheel.rl("material/default");
		public static final ResourceLocation SHADED = Flywheel.rl("material/shaded");
		public static final ResourceLocation CUTOUT = Flywheel.rl("material/cutout");
	}
}
