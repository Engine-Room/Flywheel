package com.jozufozu.flywheel.core.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.buffering.BakedModelBuilder;
import com.jozufozu.flywheel.core.model.buffering.BlockModelBuilder;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class Models {
	private static final Map<BlockState, Model> BLOCK_STATE = new HashMap<>();
	private static final Map<PartialModel, Model> PARTIAL = new HashMap<>();
	private static final Map<Pair<PartialModel, Direction>, Model> PARTIAL_DIR = new HashMap<>();

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

	public static void onReload(ReloadRenderersEvent event) {
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
