package com.jozufozu.flywheel.lib.vertex;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;

import net.minecraft.resources.ResourceLocation;

public final class VertexTypes {
	public static final BlockVertex BLOCK = VertexType.REGISTRY.registerAndGet(new BlockVertex());
	public static final PosTexNormalVertex POS_TEX_NORMAL = VertexType.REGISTRY.registerAndGet(new PosTexNormalVertex());

	private VertexTypes() {
	}

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation BLOCK_LAYOUT = Names.BLOCK.withSuffix(".vert");
		public static final ResourceLocation POS_TEX_NORMAL_LAYOUT = Names.POS_TEX_NORMAL.withSuffix(".vert");
	}

	public static final class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
