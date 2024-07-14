package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ForgeItemModelBuilder extends ItemModelBuilder {
	public static final Comparator<Model.ConfiguredMesh> GLINT_LAST = (a, b) -> {
		if (a.material()
				.transparency() == b.material()
				.transparency()) {
			return 0;
		}
		return a.material()
				.transparency() == Transparency.GLINT ? 1 : -1;
	};

	public ForgeItemModelBuilder(ItemStack itemStack, BakedModel model) {
		super(itemStack, model);
	}

	@Override
	public ForgeItemModelBuilder poseStack(PoseStack poseStack) {
		super.poseStack(poseStack);
		return this;
	}

	@Override
	public ForgeItemModelBuilder displayContext(ItemDisplayContext displayContext) {
		super.displayContext(displayContext);
		return this;
	}

	@Override
	public ForgeItemModelBuilder leftHand(boolean leftHand) {
		super.leftHand(leftHand);
		return this;
	}

	@Override
	public ForgeItemModelBuilder materialFunc(BiFunction<RenderType, Boolean, Material> materialFunc) {
		super.materialFunc(materialFunc);
		return this;
	}

	@Override
	public SimpleModel build() {
		if (displayContext == null) {
			displayContext = ItemDisplayContext.GROUND;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getItemMaterial;
		}

		ArrayList<Model.ConfiguredMesh> out = new ArrayList<>();

		BakedModelBufferer.ResultConsumer resultConsumer = (renderType, shaded, data) -> {
			Material material = materialFunc.apply(renderType, shaded);
			if (material != null) {
				var mesh = MeshHelper.blockVerticesToMesh(data, "source=ItemModelBuilder," + "itemStack=" + itemStack + ",renderType=" + renderType + ",shaded=" + shaded);
				if (mesh.vertexCount() == 0) {
					mesh.delete();
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
