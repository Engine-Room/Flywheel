package com.jozufozu.flywheel.core.model;

import static org.lwjgl.opengl.GL11.GL_QUADS;

import java.util.Collection;
import java.util.Random;

import com.jozufozu.flywheel.util.Lazy;
import com.jozufozu.flywheel.util.VirtualRenderingStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.mixin.blockrenderlayer.MixinBlockRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ModelUtil {
	private static final Lazy<ModelBlockRenderer> MODEL_RENDERER = Lazy.of(() -> new ModelBlockRenderer(Minecraft.getInstance().getBlockColors()));
	private static final Lazy<BlockModelShaper> BLOCK_MODELS = Lazy.of(() -> Minecraft.getInstance().getModelManager().getBlockModelShaper());

	public static BufferBuilder getBufferBuilderFromTemplate(BlockAndTintGetter renderWorld, RenderType layer, Collection<StructureTemplate.StructureBlockInfo> blocks) {
		PoseStack ms = new PoseStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormat.BLOCK.getIntegerSize());
		builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

//		ForgeHooksClient.setRenderLayer(layer);

		ModelBlockRenderer.enableCaching();
		for (StructureTemplate.StructureBlockInfo info : blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != RenderShape.MODEL)
				continue;
			if (!ItemBlockRenderTypes.getChunkRenderType(state).equals(layer))
				continue;

			BlockPos pos = info.pos;

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			MODEL_RENDERER.get().tesselateBlock(renderWorld, BLOCK_MODELS.get().getBlockModel(state), state, pos, ms, builder, true, random, 42, OverlayTexture.NO_OVERLAY);
			ms.popPose();
		}
		ModelBlockRenderer.clearCache();
//		ForgeHooksClient.setRenderLayer(null);

		builder.end();
		return builder;
	}
}
