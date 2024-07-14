package dev.engine_room.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@ApiStatus.NonExtendable
public abstract class ItemModelBuilder {
	final ItemStack itemStack;
	final BakedModel model;
	@Nullable
	PoseStack poseStack;
	@Nullable
	ItemDisplayContext displayContext;
	boolean leftHand;
	@Nullable
	BiFunction<RenderType, Boolean, Material> materialFunc;

	ItemModelBuilder(ItemStack itemStack, BakedModel model) {
		this.itemStack = itemStack;
		this.model = model;
	}

	public static ItemModelBuilder create(ItemStack stack, BakedModel model) {
		return FlwLibXplat.INSTANCE.createItemModelBuilder(stack, model);
	}

	public ItemModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public ItemModelBuilder displayContext(ItemDisplayContext displayContext) {
		this.displayContext = displayContext;
		return this;
	}

	public ItemModelBuilder leftHand(boolean leftHand) {
		this.leftHand = leftHand;
		return this;
	}

	public ItemModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public abstract SimpleModel build();
}
