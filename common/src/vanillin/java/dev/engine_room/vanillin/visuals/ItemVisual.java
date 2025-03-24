package dev.engine_room.vanillin.visuals;

import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.util.InstanceRecycler;
import dev.engine_room.vanillin.item.ItemModelBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

public class ItemVisual extends AbstractEntityVisual<ItemEntity> implements SimpleDynamicVisual {

	private static final ThreadLocal<RandomSource> RANDOM = ThreadLocal.withInitial(RandomSource::createNewThreadLocalInstance);

	public static final RendererReloadCache<ItemKey, Model> MODEL_CACHE = new RendererReloadCache<>(stack -> {
		return new ItemModelBuilder(stack.stack(), stack.model()).build();
	});

	private final PoseStack pPoseStack = new PoseStack();
	private final BakedModel model;
	private final boolean isSupported;

	private final InstanceRecycler<TransformedInstance> instances;

	public ItemVisual(VisualizationContext ctx, ItemEntity entity, float partialTick) {
		super(ctx, entity, partialTick);

		var item = entity.getItem();
		model = getModel(item);

		isSupported = isSupported(model);

		var key = new ItemKey(item.copy(), model);

		instances = new InstanceRecycler<>(() -> ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, MODEL_CACHE.get(key))
				.createInstance());
	}

	public static boolean isSupported(ItemEntity entity) {
		return isSupported(entity.getItem());
	}

	public static boolean isSupported(ItemStack stack) {
		// Maybe we could cache this?
		return isSupported(getModel(stack));
	}

	public static BakedModel getModel(ItemStack stack) {
		return Minecraft.getInstance()
				.getItemRenderer()
				.getItemModelShaper()
				.getItemModel(stack);
	}

	public static boolean isSupported(BakedModel model) {
		if (model.isCustomRenderer()) {
			return false;
		}

		if (model.getOverrides() != ItemOverrides.EMPTY) {
			return false;
		}

		// Check for class equality rather than instanceof to ensure subclasses are *not* handled by vanillin.
		Class<? extends BakedModel> c = model.getClass();
		if (!(c == SimpleBakedModel.class || c == MultiPartBakedModel.class || c == WeightedBakedModel.class)) {
			return false;
		}

		return true;
	}

	@Override
	public void beginFrame(Context ctx) {
		if (!isSupported || !isVisible(ctx.frustum())) {
			return;
		}

		pPoseStack.setIdentity();
		TransformStack.of(pPoseStack)
				.translate(getVisualPosition(ctx.partialTick()));

		instances.resetCount();
		ItemStack itemstack = entity.getItem();
		int i = itemstack.isEmpty() ? 187 : Item.getId(itemstack.getItem()) + itemstack.getDamageValue();
		var random = RANDOM.get();
		random.setSeed(i);
		boolean flag = model.isGui3d();
		int j = this.getRenderAmount(itemstack);
		float f = 0.25F;
		float f1 = shouldBob() ? Mth.sin(((float) entity.getAge() + ctx.partialTick()) / 10.0F + entity.bobOffs) * 0.1F + 0.1F : 0;
		float f2 = model.getTransforms()
				.getTransform(ItemDisplayContext.GROUND).scale.y();
		pPoseStack.translate(0.0F, f1 + 0.25F * f2, 0.0F);
		float f3 = entity.getSpin(ctx.partialTick());
		pPoseStack.mulPose(Axis.YP.rotation(f3));
		if (!flag) {
			float f7 = -0.0F * (float) (j - 1) * 0.5F;
			float f8 = -0.0F * (float) (j - 1) * 0.5F;
			float f9 = -0.09375F * (float) (j - 1) * 0.5F;
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
				pPoseStack.translate(0.0, 0.0, 0.09375F);
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

	public record ItemKey(ItemStack stack, BakedModel model) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			var o1 = (ItemKey) o;
			return Objects.equals(model, o1.model) && stack.hasFoil() == o1.stack.hasFoil();
		}

		@Override
		public int hashCode() {
			int out = model.hashCode();
			out = 31 * out + Boolean.hashCode(stack.hasFoil());
			return out;
		}
	}
}
