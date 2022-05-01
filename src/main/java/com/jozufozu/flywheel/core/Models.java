package com.jozufozu.flywheel.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.jozufozu.flywheel.core.model.BlockMesh;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class Models {

	public static BasicModelSupplier block(BlockState state) {
		return BLOCK_STATE.computeIfAbsent(state, it -> new BasicModelSupplier(() -> new BlockMesh(it)));
	}

	public static BasicModelSupplier partial(PartialModel partial) {
		return PARTIAL.computeIfAbsent(partial, it -> new BasicModelSupplier(() -> new BlockMesh(it)));
	}

	public static BasicModelSupplier partial(PartialModel partial, Direction dir) {
		return partial(partial, dir, () -> ModelUtil.rotateToFace(dir));
	}

	public static BasicModelSupplier partial(PartialModel partial, Direction dir, Supplier<PoseStack> modelTransform) {
		return PARTIAL_DIR.computeIfAbsent(Pair.of(dir, partial), $ -> new BasicModelSupplier(() -> new BlockMesh(partial, modelTransform.get())));
	}

	public static void onReload(ReloadRenderersEvent ignored) {
		BLOCK_STATE.clear();
		PARTIAL.clear();
		PARTIAL_DIR.clear();
	}

	private static final Map<BlockState, BasicModelSupplier> BLOCK_STATE = new HashMap<>();
	private static final Map<PartialModel, BasicModelSupplier> PARTIAL = new HashMap<>();
	private static final Map<Pair<Direction, PartialModel>, BasicModelSupplier> PARTIAL_DIR = new HashMap<>();

}
