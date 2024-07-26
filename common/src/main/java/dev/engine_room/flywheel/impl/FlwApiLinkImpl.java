package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.layout.LayoutBuilder;
import dev.engine_room.flywheel.api.registry.IdRegistry;
import dev.engine_room.flywheel.api.registry.Registry;
import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.layout.LayoutBuilderImpl;
import dev.engine_room.flywheel.impl.registry.IdRegistryImpl;
import dev.engine_room.flywheel.impl.registry.RegistryImpl;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.engine_room.flywheel.impl.visualization.VisualizerRegistryImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class FlwApiLinkImpl implements FlwApiLink {
	@Override
	public <T> Registry<T> createRegistry() {
		return new RegistryImpl<>();
	}

	@Override
	public <T> IdRegistry<T> createIdRegistry() {
		return new IdRegistryImpl<>();
	}

	@Override
	public Backend getCurrentBackend() {
		return BackendManagerImpl.currentBackend();
	}

	@Override
	public boolean isBackendOn() {
		return BackendManagerImpl.isBackendOn();
	}

	@Override
	public Backend getOffBackend() {
		return BackendManagerImpl.OFF_BACKEND;
	}

	@Override
	public Backend getDefaultBackend() {
		return BackendManagerImpl.DEFAULT_BACKEND;
	}

	@Override
	public LayoutBuilder createLayoutBuilder() {
		return new LayoutBuilderImpl();
	}

	@Override
	public boolean supportsVisualization(@Nullable LevelAccessor level) {
		return VisualizationManagerImpl.supportsVisualization(level);
	}

	@Override
	@Nullable
	public VisualizationManager getVisualizationManager(@Nullable LevelAccessor level) {
		return VisualizationManagerImpl.get(level);
	}

	@Override
	public VisualizationManager getVisualizationManagerOrThrow(@Nullable LevelAccessor level) {
		return VisualizationManagerImpl.getOrThrow(level);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityVisualizer<? super T> getVisualizer(BlockEntityType<T> type) {
		return VisualizerRegistryImpl.getVisualizer(type);
	}

	@Override
	@Nullable
	public <T extends Entity> EntityVisualizer<? super T> getVisualizer(EntityType<T> type) {
		return VisualizerRegistryImpl.getVisualizer(type);
	}

	@Override
	public <T extends BlockEntity> void setVisualizer(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer) {
		VisualizerRegistryImpl.setVisualizer(type, visualizer);
	}

	@Override
	public <T extends Entity> void setVisualizer(EntityType<T> type, EntityVisualizer<? super T> visualizer) {
		VisualizerRegistryImpl.setVisualizer(type, visualizer);
	}
}
