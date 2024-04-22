package com.jozufozu.flywheel.api.internal;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.api.registry.IdRegistry;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface FlwApiLink {
	FlwApiLink INSTANCE = DependencyInjection.load(FlwApiLink.class, "com.jozufozu.flywheel.impl.FlwApiLinkImpl");

	<T> Registry<T> createRegistry();

	<T> IdRegistry<T> createIdRegistry();

	Backend getBackend();

	boolean isBackendOn();

	Backend getOffBackend();

	Backend getDefaultBackend();

	LayoutBuilder createLayoutBuilder();

	VertexViewProvider getVertexViewProvider(VertexFormat format);

	void setVertexViewProvider(VertexFormat format, VertexViewProvider provider);

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
