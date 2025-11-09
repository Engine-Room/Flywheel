package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class FabricMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements VertexConsumer {
	private final WrapperModel wrapperModel = new WrapperModel();

	@UnknownNullability
	private RenderType defaultLayer;
	private boolean useAo;
	private boolean defaultAo;
	@Nullable
	private BufferBuilder currentDelegate;

	FabricMeshEmitterManager() {
		super(MeshEmitter::new);
	}

	public BakedModel prepareForModel(BakedModel model, RenderType defaultLayer, boolean useAo, boolean defaultAo) {
		this.defaultLayer = defaultLayer;
		this.useAo = useAo;
		this.defaultAo = defaultAo;
		wrapperModel.setWrapped(model);
		return wrapperModel;
	}

	@Override
	public SimpleModel end() {
		wrapperModel.setWrapped(null);
		return super.end();
	}

	private void prepareForGeometry(RenderMaterial material) {
		BlendMode blendMode = material.blendMode();
		RenderType layer = blendMode == BlendMode.DEFAULT ? defaultLayer : blendMode.blockRenderLayer;
		boolean shade = !material.disableDiffuse();
		TriState aoMode = material.ambientOcclusion();
		boolean ao = useAo && aoMode.orElse(defaultAo);
		currentDelegate = getBuffer(layer, shade, ao);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		if (currentDelegate != null) {
			currentDelegate.addVertex(x, y, z);
		}
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		if (currentDelegate != null) {
			currentDelegate.setColor(red, green, blue, alpha);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		if (currentDelegate != null) {
			currentDelegate.setUv(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		if (currentDelegate != null) {
			currentDelegate.setUv1(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		if (currentDelegate != null) {
			currentDelegate.setUv2(u, v);
		}
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		if (currentDelegate != null) {
			currentDelegate.setNormal(x, y, z);
		}
		return this;
	}

	@Override
	public void addVertex(float x, float y, float z, int color, float u, float v, int packedOverlay, int packedLight, float normalX, float normalY, float normalZ) {
		if (currentDelegate != null) {
			currentDelegate.addVertex(x, y, z, color, u, v, packedOverlay, packedLight, normalX, normalY, normalZ);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int packedLight, int packedOverlay) {
		if (currentDelegate != null) {
			currentDelegate.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean readAlpha) {
		if (currentDelegate != null) {
			currentDelegate.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, readAlpha);
		}
	}

	private class WrapperModel extends ForwardingBakedModel {
		private final RenderContext.QuadTransform quadTransform = quad -> {
			FabricMeshEmitterManager.this.prepareForGeometry(quad.material());
			return true;
		};

		public void setWrapped(@Nullable BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter level, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
			context.pushTransform(quadTransform);
			super.emitBlockQuads(level, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
