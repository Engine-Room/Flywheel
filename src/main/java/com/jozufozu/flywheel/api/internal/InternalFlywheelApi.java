package com.jozufozu.flywheel.api.internal;

import java.lang.reflect.Constructor;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.layout.LayoutBuilder;
import com.jozufozu.flywheel.api.registry.IdRegistry;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.api.visualization.BlockEntityVisualizer;
import com.jozufozu.flywheel.api.visualization.EntityVisualizer;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public interface InternalFlywheelApi {
	InternalFlywheelApi INSTANCE = load();

	// Adapted from https://github.com/CaffeineMC/sodium-fabric/blob/bf4fc9dab16e1cca07b2f23a1201c9bf237c8044/src/api/java/net/caffeinemc/mods/sodium/api/internal/DependencyInjection.java
	private static InternalFlywheelApi load() {
		Class<InternalFlywheelApi> apiClass = InternalFlywheelApi.class;
		Class<?> implClass;

		try {
			implClass = Class.forName("com.jozufozu.flywheel.impl.InternalFlywheelImpl");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not find implementation", e);
		}

		if (!apiClass.isAssignableFrom(implClass)) {
			throw new RuntimeException("Class %s does not implement interface %s"
					.formatted(implClass.getName(), apiClass.getName()));
		}

		Constructor<?> implConstructor;

		try {
			implConstructor = implClass.getConstructor();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not find default constructor", e);
		}

		Object implInstance;

		try {
			implInstance = implConstructor.newInstance();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Could not instantiate implementation", e);
		}

		return apiClass.cast(implInstance);
	}

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

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);

	Map<String, ModelPart> getModelPartChildren(ModelPart part);

	void compileModelPart(ModelPart part, PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha);
}
