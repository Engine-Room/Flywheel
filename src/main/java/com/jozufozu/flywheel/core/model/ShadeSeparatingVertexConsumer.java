package com.jozufozu.flywheel.core.model;

import java.util.Random;
import java.util.function.Supplier;

import com.jozufozu.flywheel.fabric.model.FabricModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class ShadeSeparatingVertexConsumer implements VertexConsumer {
	private final ShadeSeparatingBakedModel modelWrapper = new ShadeSeparatingBakedModel();
	protected VertexConsumer shadedConsumer;
	protected VertexConsumer unshadedConsumer;
	protected VertexConsumer activeConsumer;

	public void prepare(VertexConsumer shadedConsumer, VertexConsumer unshadedConsumer) {
		this.shadedConsumer = shadedConsumer;
		this.unshadedConsumer = unshadedConsumer;
	}

	public void clear() {
		shadedConsumer = null;
		unshadedConsumer = null;
		activeConsumer = null;
	}

	public BakedModel wrapModel(BakedModel model) {
		modelWrapper.setWrapped(model);
		return modelWrapper;
	}

	protected void setActiveConsumer(boolean shaded) {
		activeConsumer = shaded ? shadedConsumer : unshadedConsumer;
	}

	@Override
	public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
		if (quad.isShade()) {
			shadedConsumer.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		} else {
			unshadedConsumer.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		}
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		activeConsumer.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		activeConsumer.color(red, green, blue, alpha);
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		activeConsumer.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		activeConsumer.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		activeConsumer.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		activeConsumer.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex() {
		activeConsumer.endVertex();
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		activeConsumer.defaultColor(red, green, blue, alpha);
	}

	@Override
	public void unsetDefaultColor() {
		activeConsumer.unsetDefaultColor();
	}

	private class ShadeSeparatingBakedModel extends ForwardingBakedModel {
		private final QuadTransform quadTransform = quad -> {
			ShadeSeparatingVertexConsumer.this.setActiveConsumer(FabricModelUtil.isShaded(quad));
			return true;
		};

		private void setWrapped(BakedModel model) {
			wrapped = model;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			context.pushTransform(quadTransform);
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
