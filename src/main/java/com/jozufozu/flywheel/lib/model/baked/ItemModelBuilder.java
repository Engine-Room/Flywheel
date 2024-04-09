package com.jozufozu.flywheel.lib.model.baked;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemModelBuilder {
	private final ItemStack itemStack;
	@Nullable
	private PoseStack poseStack;
	@Nullable
	private ItemDisplayContext displayContext;
	private boolean leftHand;
	private int seed = 42;
	@Nullable
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public ItemModelBuilder(ItemStack itemStack) {
		this.itemStack = itemStack;
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

	public ItemModelBuilder seed(int seed) {
		this.seed = seed;
		return this;
	}

	public ItemModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public SimpleModel build() {
		if (displayContext == null) {
			displayContext = ItemDisplayContext.GROUND;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getItemMaterial;
		}

		var out = ImmutableList.<Model.ConfiguredMesh>builder();

		ResultConsumer resultConsumer = (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=ItemModelBuilder," + "itemStack=" + itemStack + ",renderType=" + renderType + ",shaded=" + shaded);
				out.add(new Model.ConfiguredMesh(material, mesh));
			}
		};

		var model = Minecraft.getInstance()
				.getItemRenderer()
				.getModel(itemStack, null, null, seed);

		BakedModelBufferer.bufferItem(model, itemStack, displayContext, leftHand, poseStack, resultConsumer);

		return new SimpleModel(out.build());
	}
}
