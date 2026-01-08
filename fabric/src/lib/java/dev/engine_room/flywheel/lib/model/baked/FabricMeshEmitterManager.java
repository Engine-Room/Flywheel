package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.render.BlockVertexConsumerProvider;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

class FabricMeshEmitterManager extends MeshEmitterManager<FabricMeshEmitter> implements BlockVertexConsumerProvider {
	private final WrapperModel wrapperModel = new WrapperModel();

	private boolean useAo;
	private boolean defaultAo;

	FabricMeshEmitterManager() {
		super(FabricMeshEmitter::new);
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
		boolean shade = quad.diffuseShade();
		TriState aoMode = quad.ambientOcclusion();
		boolean ao = useAo && aoMode.orElse(defaultAo);

		for (FabricMeshEmitter emitter : emitterMap.values()) {
			emitter.prepareForGeometry(shade, ao);
		}
	}

	@Override
	public FabricMeshEmitter getBuffer(ChunkSectionLayer layer) {
		return getEmitter(layer);
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
