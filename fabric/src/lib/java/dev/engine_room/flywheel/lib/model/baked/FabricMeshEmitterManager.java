package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Predicate;

import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class FabricMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements VertexConsumer {
	private final WrapperModel wrapperModel = new WrapperModel();

	@UnknownNullability
	private ChunkSectionLayer defaultLayer;
	private boolean useAo;
	private boolean defaultAo;
	@Nullable
	private BufferBuilder currentDelegate;

	FabricMeshEmitterManager() {
		super(MeshEmitter::new);
	}

	public BlockStateModel prepareForModel(BlockStateModel model, boolean useAo, boolean defaultAo) {
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

	private void prepareForGeometry(QuadView quad) {
		ChunkSectionLayer renderLayer = quad.renderLayer();
		boolean shade = !quad.diffuseShade();
		TriState aoMode = quad.ambientOcclusion();
		boolean ao = useAo && aoMode.orElse(defaultAo);
		currentDelegate = getBuffer(renderLayer, shade, ao);
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
	public VertexConsumer setColor(int color) {
		if (currentDelegate != null) {
			currentDelegate.setColor(color);
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
	public VertexConsumer setLineWidth(float f) {
		if (currentDelegate != null) {
			currentDelegate.setLineWidth(f);
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
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay) {
		if (currentDelegate != null) {
			currentDelegate.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay);
		}
	}

	private class WrapperModel extends WrapperBlockStateModel {
		private final QuadTransform quadTransform = quad -> {
			FabricMeshEmitterManager.this.prepareForGeometry(quad);
			return true;
		};

		public void setWrapped(@Nullable BlockStateModel wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
			emitter.pushTransform(quadTransform);
			super.emitQuads(emitter, blockView, pos, state, random, cullTest);
			emitter.popTransform();
		}
	}
}
