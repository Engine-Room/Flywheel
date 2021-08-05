package com.jozufozu.flywheel.core.model;

import static org.lwjgl.opengl.GL11.GL_QUADS;


import java.util.Collection;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.util.Lazy;

public class ModelUtil {
	private static final Lazy<BlockModelRenderer> MODEL_RENDERER = Lazy.of(() -> new BlockModelRenderer(Minecraft.getInstance().getBlockColors()));
	private static final Lazy<BlockModelShapes> BLOCK_MODELS = Lazy.of(() -> Minecraft.getInstance().getModelManager().getBlockModelShaper());

	public static BufferBuilder getBufferBuilderFromTemplate(IBlockDisplayReader renderWorld, RenderType layer, Collection<Template.BlockInfo> blocks) {
		MatrixStack ms = new MatrixStack();
		Random random = new Random();
		BufferBuilder builder = new BufferBuilder(DefaultVertexFormats.BLOCK.getIntegerSize());
		builder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);

		ForgeHooksClient.setRenderLayer(layer);
		BlockModelRenderer.enableCaching();
		for (Template.BlockInfo info : blocks) {
			BlockState state = info.state;

			if (state.getRenderShape() != BlockRenderType.MODEL)
				continue;
			if (!RenderTypeLookup.canRenderInLayer(state, layer))
				continue;

			BlockPos pos = info.pos;

			ms.pushPose();
			ms.translate(pos.getX(), pos.getY(), pos.getZ());
			MODEL_RENDERER.get().renderModel(renderWorld, BLOCK_MODELS.get().getBlockModel(state), state, pos, ms, builder, true,
					random, 42, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			ms.popPose();
		}
		BlockModelRenderer.clearCache();
		ForgeHooksClient.setRenderLayer(null);

		builder.end();
		return builder;
	}
}
