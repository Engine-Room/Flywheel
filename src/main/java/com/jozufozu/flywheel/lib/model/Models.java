package com.jozufozu.flywheel.lib.model;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class Models {
	private static final ModelCache<BlockState> BLOCK_STATE = new ModelCache<>(it -> new BlockModelBuilder(it).build());
	private static final ModelCache<PartialModel> PARTIAL = new ModelCache<>(it -> new BakedModelBuilder(it.get()).build());
	private static final ModelCache<Pair<PartialModel, Direction>> PARTIAL_DIR = new ModelCache<>(it -> new BakedModelBuilder(it.first().get()).poseStack(createRotation(it.second())).build());

	private Models() {
	}

	public static Model block(BlockState state) {
		return BLOCK_STATE.get(state);
	}

	public static Model partial(PartialModel partial) {
		return PARTIAL.get(partial);
	}

	public static Model partial(PartialModel partial, Direction dir) {
		return PARTIAL_DIR.get(Pair.of(partial, dir));
	}

	private static PoseStack createRotation(Direction facing) {
		PoseStack stack = new PoseStack();
		TransformStack.of(stack)
				.center()
				.rotateToFace(facing.getOpposite())
				.uncenter();
		return stack;
	}
}
