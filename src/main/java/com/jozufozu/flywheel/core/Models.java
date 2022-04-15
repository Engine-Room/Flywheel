package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Models {

	public static ModelSupplier block(BlockState state) {
		return BLOCK_STATE.computeIfAbsent(state, it -> new ModelSupplier(() -> new BlockModel(it)));
	}

	public static ModelSupplier partial(PartialModel partial) {
		return PARTIAL.computeIfAbsent(partial, it -> new ModelSupplier(() -> new BlockModel(it)));
	}

	public static ModelSupplier partial(PartialModel partial, Direction dir) {
		return partial(partial, dir, () -> ModelUtil.rotateToFace(dir));
	}

	public static ModelSupplier partial(PartialModel partial, Direction dir, Supplier<PoseStack> modelTransform) {
		return PARTIAL_DIR.computeIfAbsent(Pair.of(dir, partial), $ -> new ModelSupplier(() -> new BlockModel(partial, modelTransform.get())));
	}

	public static void onReload(ReloadRenderersEvent ignored) {
		BLOCK_STATE.clear();
		PARTIAL.clear();
		PARTIAL_DIR.clear();
	}

	private static final Map<BlockState, ModelSupplier> BLOCK_STATE = new HashMap<>();
	private static final Map<PartialModel, ModelSupplier> PARTIAL = new HashMap<>();
	private static final Map<Pair<Direction, PartialModel>, ModelSupplier> PARTIAL_DIR = new HashMap<>();

}
