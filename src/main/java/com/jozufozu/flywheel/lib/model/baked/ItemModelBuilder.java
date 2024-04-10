package com.jozufozu.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.vertex.VertexView;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ResultConsumer;
import com.jozufozu.flywheel.lib.vertex.NoOverlayVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemModelBuilder {
	public static final Comparator<Model.ConfiguredMesh> GLINT_LAST = (a, b) -> {
		if (a.material()
				.transparency() == b.material()
				.transparency()) {
			return 0;
		}
		return a.material()
				.transparency() == Transparency.GLINT ? 1 : -1;
	};

	private final ItemStack itemStack;
	private final BakedModel model;
	@Nullable
	private PoseStack poseStack;
	@Nullable
	private ItemDisplayContext displayContext;
	private boolean leftHand;
	@Nullable
	private BiFunction<RenderType, Boolean, Material> materialFunc;

	public ItemModelBuilder(ItemStack itemStack, BakedModel model) {
		this.itemStack = itemStack;
		this.model = model;
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

	public SimpleModel build() {
		if (displayContext == null) {
			displayContext = ItemDisplayContext.GROUND;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getItemMaterial;
		}

		ArrayList<Model.ConfiguredMesh> out = new ArrayList<>();

		ResultConsumer resultConsumer = (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				VertexView vertexView = new NoOverlayVertexView();
				MemoryBlock meshData = ModelUtil.convertVanillaBuffer(data, vertexView);
				var mesh = new SimpleMesh(vertexView, meshData, "source=ItemModelBuilder," + "itemStack=" + itemStack + ",renderType=" + renderType + ",shaded=" + shaded);
				if (mesh.vertexCount() == 0) {
					mesh.delete();
					return;
				} else {
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			}
		};

		BakedModelBufferer.bufferItem(model, itemStack, displayContext, leftHand, poseStack, resultConsumer);

		out.sort(GLINT_LAST);

		return new SimpleModel(ImmutableList.copyOf(out));
	}
}
