package dev.engine_room.flywheel.impl.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.impl.compat.CompatMods;
import dev.engine_room.flywheel.impl.compat.SodiumCompat;
import dev.engine_room.flywheel.impl.extension.BlockEntityTypeExtension;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
abstract class BlockEntityTypeMixin<T extends BlockEntity> implements BlockEntityTypeExtension<T> {
	@Unique
	@Nullable
	private BlockEntityVisualizer<? super T> flywheel$visualizer;

	@Unique
	@Nullable
	private Object flywheel$sodiumPredicate;

	@Override
	@Nullable
	public BlockEntityVisualizer<? super T> flywheel$getVisualizer() {
		return flywheel$visualizer;
	}

	@Override
	public void flywheel$setVisualizer(@Nullable BlockEntityVisualizer<? super T> visualizer) {
		if (CompatMods.SODIUM.isLoaded() && !CompatMods.EMBEDDIUM.isLoaded()) {
			if (flywheel$visualizer == null && visualizer != null) {
				flywheel$sodiumPredicate = SodiumCompat.forBlockEntityType((BlockEntityType<?>) (Object) this);
			} else if (flywheel$visualizer != null && visualizer == null && flywheel$sodiumPredicate != null) {
				SodiumCompat.removePredicate((BlockEntityType<?>) (Object) this, flywheel$sodiumPredicate);
			}
		}
		this.flywheel$visualizer = visualizer;
	}
}
