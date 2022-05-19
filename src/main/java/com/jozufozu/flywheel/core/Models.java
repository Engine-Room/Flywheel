package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.ModelSupplier;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Models {

	public static ModelSupplier block(BlockState state) {
		return BLOCK_STATE.apply(state);
	}

	public static ModelSupplier partial(PartialModel partial) {
		return PARTIAL.apply(partial);
	}

	public static ModelSupplier partial(PartialModel partial, Direction dir, Supplier<PoseStack> modelTransform) {
		return PARTIAL_DIR.computeIfAbsent(Pair.of(dir, partial), $ -> new SimpleModelSupplier(() -> new BlockModel(partial.get(), Blocks.AIR.defaultBlockState(), modelTransform.get())));
	}

	private static final Function<BlockState, ModelSupplier> BLOCK_STATE = Util.memoize(it -> new SimpleModelSupplier(() -> new BlockModel(it)));
	private static final Function<PartialModel, ModelSupplier> PARTIAL = Util.memoize(it -> new SimpleModelSupplier(() -> new BlockModel(it.get(), Blocks.AIR.defaultBlockState())));
	private static final Map<Pair<Direction, PartialModel>, ModelSupplier> PARTIAL_DIR = new HashMap<>();

}
