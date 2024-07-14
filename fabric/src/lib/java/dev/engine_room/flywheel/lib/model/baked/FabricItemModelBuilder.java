package dev.engine_room.flywheel.lib.model.baked;

import java.util.List;
import java.util.function.BiFunction;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FabricItemModelBuilder extends ItemModelBuilder {
	public FabricItemModelBuilder(ItemStack itemStack, BakedModel model) {
		super(itemStack, model);
	}

	@Override
	public FabricItemModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public FabricItemModelBuilder displayContext(ItemDisplayContext displayContext) {
		super.displayContext(displayContext);
		return this;
	}

	@Override
	public FabricItemModelBuilder leftHand(boolean leftHand) {
		super.leftHand(leftHand);
		return this;
	}

	@Override
	public FabricItemModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		// TODO
		return new SimpleModel(List.of());
	}
}
