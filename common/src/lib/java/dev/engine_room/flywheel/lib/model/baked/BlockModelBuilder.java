package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@ApiStatus.NonExtendable
public abstract class BlockModelBuilder {
	final BlockState state;
	@Nullable
	BlockAndTintGetter level;
	@Nullable
	PoseStack poseStack;
	@Nullable
	BiFunction<RenderType, Boolean, Material> materialFunc;

	BlockModelBuilder(BlockState state) {
		this.state = state;
	}

	public static BlockModelBuilder create(BlockState state) {
		return FlwLibXplat.INSTANCE.createBlockModelBuilder(state);
	}

	public BlockModelBuilder level(BlockAndTintGetter level) {
		this.level = level;
		return this;
	}

	public BlockModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public BlockModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
