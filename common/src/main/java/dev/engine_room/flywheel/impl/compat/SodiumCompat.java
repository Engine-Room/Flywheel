package dev.engine_room.flywheel.impl.compat;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderHandler;
import net.caffeinemc.mods.sodium.api.blockentity.BlockEntityRenderPredicate;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class SodiumCompat {
	public static <T extends BlockEntity> Object addPredicate(BlockEntityType<T> type) {
		BlockEntityRenderPredicate<T> predicate = (getter, pos, be) -> VisualizationHelper.tryAddBlockEntity(be);
		BlockEntityRenderHandler.instance().addRenderPredicate(type, predicate);
		return predicate;
	}

	public static <T extends BlockEntity> void removePredicate(BlockEntityType<T> type, Object predicate) {
		BlockEntityRenderHandler.instance().removeRenderPredicate(type, (BlockEntityRenderPredicate<T>) predicate);
	}
}
