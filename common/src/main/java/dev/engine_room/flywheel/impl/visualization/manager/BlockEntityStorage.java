package dev.engine_room.flywheel.impl.visualization.manager;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.BlockEntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.impl.visualization.storage.Storage;
import dev.engine_room.flywheel.lib.visual.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class BlockEntityStorage extends Storage<BlockEntity> {
	private final Long2ObjectMap<BlockEntityVisual<?>> posLookup = new Long2ObjectOpenHashMap<>();

	public BlockEntityStorage(Supplier<VisualizationContext> visualizationContextSupplier) {
		super(visualizationContextSupplier);
	}

	@Nullable
	public BlockEntityVisual<?> visualAtPos(long pos) {
		return posLookup.get(pos);
	}

	@Override
	public boolean willAccept(BlockEntity blockEntity) {
		if (blockEntity.isRemoved()) {
			return false;
		}

		if (!VisualizationHelper.canVisualize(blockEntity)) {
			return false;
		}

		Level level = blockEntity.getLevel();
		if (level == null) {
			return false;
		}

		if (level.isEmptyBlock(blockEntity.getBlockPos())) {
			return false;
		}

		BlockPos pos = blockEntity.getBlockPos();
		BlockGetter existingChunk = level.getChunkForCollisions(pos.getX() >> 4, pos.getZ() >> 4);
		return existingChunk != null;
	}

	@Override
	@Nullable
	protected BlockEntityVisual<?> createRaw(BlockEntity obj, float partialTick) {
		var visualizer = VisualizationHelper.getVisualizer(obj);
		if (visualizer == null) {
			return null;
		}

		var visual = visualizer.createVisual(visualizationContextSupplier.get(), obj, partialTick);

		BlockPos blockPos = obj.getBlockPos();
		posLookup.put(blockPos.asLong(), visual);

		return visual;
	}

	@Override
	public void remove(BlockEntity obj) {
		super.remove(obj);
		posLookup.remove(obj.getBlockPos()
				.asLong());
	}
}
