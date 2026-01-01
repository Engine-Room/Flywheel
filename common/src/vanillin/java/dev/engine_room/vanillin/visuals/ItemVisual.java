package dev.engine_room.vanillin.visuals;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.util.InstanceRecycler;
import dev.engine_room.vanillin.item.ItemModels;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

public class ItemVisual extends AbstractEntityVisual<ItemEntity> implements SimpleDynamicVisual {

	private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance);

	private final PoseStack pPoseStack = new PoseStack();
	private BakedModel bakedModel;
	private ItemStack currentStack;

	private InstanceRecycler<TransformedInstance> instances;

	public ItemVisual(VisualizationContext ctx, ItemEntity entity, float partialTick) {
		super(ctx, entity, partialTick);

		currentStack = entity.getItem();
		bakedModel = ItemModels.getModel(currentStack);
		var model =  ItemModels.get(level, currentStack, ItemDisplayContext.GROUND);
		instances = new InstanceRecycler<>(() -> ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance());

		animate(partialTick);
	}

	public static boolean isSupported(ItemEntity entity) {
		return ItemModels.isSupported(entity.getItem());
	}

	@Override
	public void beginFrame(Context ctx) {
		if (!isVisible(ctx.frustum())) {
			return;
		}

		animate(ctx.partialTick());
	}

	private void animate(float partialTick) {
		pPoseStack.setIdentity();
		TransformStack.of(pPoseStack)
				.translate(getVisualPosition(partialTick));

		ItemStack itemstack = entity.getItem();
		if (!ItemStack.matches(itemstack, currentStack)) {
			instances.delete();
			currentStack = itemstack.copy();
			bakedModel = ItemModels.getModel(currentStack);
			var model =  ItemModels.get(level, currentStack, ItemDisplayContext.GROUND);
			instances = new InstanceRecycler<>(() -> visualizationContext.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, model)
					.createInstance());
		}
		instances.resetCount();

		int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
		var random = RANDOM.get();
		random.setSeed(i);
		boolean flag = bakedModel.isGui3d();
		int j = this.getRenderAmount(itemstack);
		float f = 0.25F;
		float f1 = shouldBob() ? Mth.sin(((float) entity.getAge() + partialTick) / 10.0F + entity.bobOffs) * 0.1F + 0.1F : 0;
		float groundScaleX = bakedModel.getTransforms().ground.scale.y();
		float groundScaleY = bakedModel.getTransforms().ground.scale.y();
		float groundScaleZ = bakedModel.getTransforms().ground.scale.y();
		pPoseStack.translate(0.0F, f1 + 0.25F * groundScaleZ, 0.0F);
		float f3 = entity.getSpin(partialTick);
		pPoseStack.mulPose(Axis.YP.rotation(f3));
		if (!flag) {
			float f7 = -0.0F * (float) (j - 1) * 0.5F * groundScaleX;
			float f8 = -0.0F * (float) (j - 1) * 0.5F * groundScaleY;
			float f9 = -0.09375F * (float) (j - 1) * 0.5F * groundScaleZ;
			pPoseStack.translate(f7, f8, f9);
		}

		int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, entity.blockPosition()), level.getBrightness(LightLayer.SKY, entity.blockPosition()));

		for (int k = 0; k < j; ++k) {
			pPoseStack.pushPose();
			if (k > 0) {
				if (flag) {
					float f11 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float f13 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					float f10 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
					pPoseStack.translate(shouldSpreadItems() ? f11 : 0, shouldSpreadItems() ? f13 : 0, shouldSpreadItems() ? f10 : 0);
				} else {
					float f12 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					float f14 = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
					pPoseStack.translate(shouldSpreadItems() ? f12 : 0, shouldSpreadItems() ? f14 : 0, 0.0D);
				}
			}

			instances.get()
					.setTransform(pPoseStack.last())
					.light(light)
					.setChanged();
			pPoseStack.popPose();
			if (!flag) {
				pPoseStack.translate(0.0, 0.0, 0.09375F * groundScaleZ);
			}
		}

		instances.discardExtra();
	}

	protected int getRenderAmount(ItemStack pStack) {
		int i = 1;
		if (pStack.getCount() > 48) {
			i = 5;
		} else if (pStack.getCount() > 32) {
			i = 4;
		} else if (pStack.getCount() > 16) {
			i = 3;
		} else if (pStack.getCount() > 1) {
			i = 2;
		}

		return i;
	}

	/**
	 * @return If items should spread out when rendered in 3D
	 */
	public boolean shouldSpreadItems() {
		return true;
	}

	/**
	 * @return If items should have a bob effect
	 */
	public boolean shouldBob() {
		return true;
	}

	@Override
	protected void _delete() {
		instances.delete();
	}

}
