package dev.engine_room.flywheel.vanilla;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;

public class TntMinecartVisual<T extends MinecartTNT> extends MinecartVisual<T> {
	private static final int WHITE_OVERLAY = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);

	public TntMinecartVisual(VisualizationContext ctx, T entity, float partialTick) {
		super(ctx, entity, partialTick, TNT_BODY_MODEL);
	}

	@Override
	protected void updateContents(TransformedInstance contents, PoseStack stack, float partialTick) {
		int fuseTime = entity.getFuse();
		if (fuseTime > -1 && (float) fuseTime - partialTick + 1.0F < 10.0F) {
			float f = 1.0F - ((float) fuseTime - partialTick + 1.0F) / 10.0F;
			f = Mth.clamp(f, 0.0F, 1.0F);
			f *= f;
			f *= f;
			float scale = 1.0F + f * 0.3F;
			stack.scale(scale, scale, scale);
		}

		int overlay;
		if (fuseTime > -1 && fuseTime / 5 % 2 == 0) {
			overlay = WHITE_OVERLAY;
		} else {
			overlay = OverlayTexture.NO_OVERLAY;
		}

		contents.setTransform(stack)
				.overlay(overlay)
				.setChanged();
	}
}
