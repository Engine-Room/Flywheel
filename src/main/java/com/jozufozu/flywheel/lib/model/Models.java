package com.jozufozu.flywheel.lib.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.lib.model.buffering.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.buffering.BlockModelBuilder;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class Models {
	private static final Map<BlockState, Model> BLOCK_STATE = new ConcurrentHashMap<>();
	private static final Map<PartialModel, Model> PARTIAL = new ConcurrentHashMap<>();
	private static final Map<Pair<PartialModel, Direction>, Model> PARTIAL_DIR = new ConcurrentHashMap<>();

	public static Model block(BlockState state) {
		return BLOCK_STATE.computeIfAbsent(state, it -> new BlockModelBuilder(it).build());
	}

	public static Model partial(PartialModel partial) {
		return PARTIAL.computeIfAbsent(partial, it -> new BakedModelBuilder(it.get()).build());
	}

	public static Model partial(PartialModel partial, Direction dir) {
		return PARTIAL_DIR.computeIfAbsent(Pair.of(partial, dir), it -> new BakedModelBuilder(it.first().get()).poseStack(createRotation(it.second())).build());
	}

	public static PoseStack createRotation(Direction facing) {
		PoseStack stack = new PoseStack();
		TransformStack.cast(stack)
				.centre()
				.rotateToFace(facing.getOpposite())
				.unCentre();
		return stack;
	}

	public static void onReloadRenderers(ReloadRenderersEvent event) {
		deleteAll(BLOCK_STATE.values());
		deleteAll(PARTIAL.values());
		deleteAll(PARTIAL_DIR.values());

		BLOCK_STATE.clear();
		PARTIAL.clear();
		PARTIAL_DIR.clear();
	}

	private static void deleteAll(Collection<Model> values) {
		values.forEach(Model::delete);
	}
}
