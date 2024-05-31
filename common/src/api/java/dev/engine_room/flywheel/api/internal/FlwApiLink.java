package dev.engine_room.flywheel.api.internal;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.api.registry.IdRegistry;
import dev.engine_room.flywheel.api.registry.Registry;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface FlwApiLink {
	FlwApiLink INSTANCE = DependencyInjection.load(FlwApiLink.class, "dev.engine_room.flywheel.impl.FlwApiLinkImpl");

	<T> Registry<T> createRegistry();

	<T> IdRegistry<T> createIdRegistry();

	Backend getBackend();

	boolean isBackendOn();

	Backend getOffBackend();

	Backend getDefaultBackend();

	LayoutBuilder createLayoutBuilder();

	boolean supportsVisualization(@Nullable LevelAccessor level);

	@Nullable
	VisualizationManager getVisualizationManager(@Nullable LevelAccessor level);

	VisualizationManager getVisualizationManagerOrThrow(@Nullable LevelAccessor level);

	@Nullable
	<T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type);

	@Nullable
	<T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type);

	<T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer);

	<T extends Entity> void setVisualizer(EntityType<T> type, EntityVisualizer<? super T> visualizer);
}
