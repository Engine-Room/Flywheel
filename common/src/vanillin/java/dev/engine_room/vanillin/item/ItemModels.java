package dev.engine_room.vanillin.item;

import java.util.Objects;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemModels {
	public static final RendererReloadCache<ItemKey, Model> MODEL_CACHE = new RendererReloadCache<>(stack -> {
		return new ItemModelBuilder(stack.stack(), stack.model()).displayContext(stack.context).build();
	});

	public static Model get(ItemKey key) {
		return MODEL_CACHE.get(key);
	}

	public static Model get(ItemStack stack, ItemDisplayContext context) {
		return get(new ItemKey(stack.copy(), getModel(stack), context));
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

	// TODO: Do we need the BakedModel in here?
	public record ItemKey(ItemStack stack, BakedModel model, ItemDisplayContext context) {
		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			var o1 = (ItemKey) o;
			return Objects.equals(model, o1.model) && stack.hasFoil() == o1.stack.hasFoil() && context == o1.context;
		}

		@Override
		public int hashCode() {
			int out = model.hashCode();
			out = 31 * out + Boolean.hashCode(stack.hasFoil());
			out = 31 * out + context.hashCode();
			return out;
		}
	}
}
