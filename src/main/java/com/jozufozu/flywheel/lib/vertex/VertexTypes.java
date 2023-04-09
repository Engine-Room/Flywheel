package com.jozufozu.flywheel.lib.vertex;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class VertexTypes {
	public static final BlockVertex BLOCK = VertexType.REGISTRY.registerAndGet(new BlockVertex());
	public static final PosTexNormalVertex POS_TEX_NORMAL = VertexType.REGISTRY.registerAndGet(new PosTexNormalVertex());

	@ApiStatus.Internal
	public static void init() {
	}

	public static final class Files {
		public static final ResourceLocation BLOCK_LAYOUT = ResourceUtil.subPath(Names.BLOCK, ".vert");
		public static final ResourceLocation POS_TEX_NORMAL_LAYOUT = ResourceUtil.subPath(Names.POS_TEX_NORMAL, ".vert");
	}

	public static final class Names {
		public static final ResourceLocation BLOCK = Flywheel.rl("layout/block");
		public static final ResourceLocation POS_TEX_NORMAL = Flywheel.rl("layout/pos_tex_normal");
	}
}
