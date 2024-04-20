package com.jozufozu.flywheel.lib.internal;

import java.lang.reflect.Constructor;

import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.BlockModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.MultiBlockModelBuilder;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface FlywheelLibPlatform {
	FlywheelLibPlatform INSTANCE = load();

	// Adapted from https://github.com/CaffeineMC/sodium-fabric/blob/bf4fc9dab16e1cca07b2f23a1201c9bf237c8044/src/api/java/net/caffeinemc/mods/sodium/api/internal/DependencyInjection.java
	private static FlywheelLibPlatform load() {
		Class<FlywheelLibPlatform> apiClass = FlywheelLibPlatform.class;
		Class<?> implClass;

		try {
			implClass = Class.forName("com.jozufozu.flywheel.impl.FlywheelLibPlatformImpl");
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

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);

	BlockRenderDispatcher createVanillaRenderer();

	BakedModelBuilder bakedModelBuilder(BakedModel bakedModel);

	BlockModelBuilder blockModelBuilder(BlockState state);

	MultiBlockModelBuilder multiBlockModelBuilder(BlockAndTintGetter level, Iterable<BlockPos> positions);
}
