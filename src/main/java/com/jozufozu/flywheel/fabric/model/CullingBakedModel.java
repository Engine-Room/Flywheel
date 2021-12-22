package com.jozufozu.flywheel.fabric.model;

import java.util.Random;
import java.util.function.Supplier;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CullingBakedModel extends ForwardingBakedModel {
	private static final ThreadLocal<CullingBakedModel> THREAD_LOCAL = ThreadLocal.withInitial(CullingBakedModel::new);

	protected int completionFlags = 0;
	protected int resultFlags = 0;

	protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	public static BakedModel wrap(BakedModel model) {
		if (!FabricModelUtil.FREX_LOADED && !((FabricBakedModel) model).isVanillaAdapter()) {
			CullingBakedModel wrapper = THREAD_LOCAL.get();
			wrapper.wrapped = model;
			return wrapper;
		}
		return model;
	}

	protected CullingBakedModel() {
	}

	@Override
	public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		completionFlags = 0;
		resultFlags = 0;
		context.pushTransform(quad -> {
			Direction cullFace = quad.cullFace();
			if (cullFace != null) {
				int mask = 1 << cullFace.ordinal();
				if ((completionFlags & mask) == 0) {
					completionFlags |= mask;
					if (Block.shouldRenderFace(state, blockView, pos, cullFace, mutablePos.setWithOffset(pos, cullFace))) {
						resultFlags |= mask;
						return true;
					} else {
						return false;
					}
				} else {
					return (resultFlags & mask) != 0;
				}
			}
			return true;
		});
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();
	}
}
