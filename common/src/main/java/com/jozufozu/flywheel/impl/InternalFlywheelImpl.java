package com.jozufozu.flywheel.impl;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.internal.InternalFlywheelApi;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.api.registry.IdRegistry;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.impl.extension.PoseStackExtension;
import com.jozufozu.flywheel.impl.layout.LayoutBuilderImpl;
import com.jozufozu.flywheel.impl.registry.IdRegistryImpl;
import com.jozufozu.flywheel.impl.registry.RegistryImpl;
import com.jozufozu.flywheel.impl.vertex.VertexViewProviderRegistryImpl;
import com.jozufozu.flywheel.impl.visualization.VisualizationManagerImpl;
import com.jozufozu.flywheel.impl.visualization.VisualizerRegistryImpl;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class InternalFlywheelImpl implements InternalFlywheelApi {
	@Override
	public <T> Registry<T> createRegistry() {
		return new RegistryImpl<>();
	}

	@Override
	public <T> IdRegistry<T> createIdRegistry() {
		return new IdRegistryImpl<>();
	}

	@Override
	public Backend getBackend() {
		return BackendManagerImpl.getBackend();
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
	public VertexViewProvider getVertexViewProvider(VertexFormat format) {
		return VertexViewProviderRegistryImpl.getProvider(format);
	}

	@Override
	public void setVertexViewProvider(VertexFormat format, VertexViewProvider provider) {
		VertexViewProviderRegistryImpl.setProvider(format, provider);
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
