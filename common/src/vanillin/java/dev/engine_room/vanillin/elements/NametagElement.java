package dev.engine_room.vanillin.elements;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.vanillin.text.TextLayer;
import dev.engine_room.vanillin.text.TextLayers;
import dev.engine_room.vanillin.text.TextVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class NametagElement extends AbstractVisual implements SimpleDynamicVisual {
	private final TextVisual text;
	private final Entity entity;

	private final Matrix4f matrix = new Matrix4f();

	public NametagElement(VisualizationContext ctx, Entity entity, float partialTick) {
		super(ctx, entity.level(), partialTick);

		this.entity = entity;

		text = new TextVisual(ctx.instancerProvider());
	}

	@Override
	public void beginFrame(Context ctx) {
		var displayName = entity.getDisplayName();

		var sequence = displayName.getVisualOrderText();

		boolean notDiscrete = !entity.isDiscrete();

		float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
		int j = (int) (g * 255.0F) << 24;

		List<TextLayer> layers = new ArrayList<>();
		layers.add(TextLayers.normal(553648127, notDiscrete ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, 1));

		if (notDiscrete) {
			layers.add(TextLayers.normal(-1, Font.DisplayMode.NORMAL));
		}

		float f = entity.getNameTagOffsetY();

		float x = (float) (Mth.lerp(ctx.partialTick(), entity.xOld, entity.getX()) - visualizationContext.renderOrigin()
				.getX());
		float y = (float) (Mth.lerp(ctx.partialTick(), entity.yOld, entity.getY()) - visualizationContext.renderOrigin()
				.getY()) + f;
		float z = (float) (Mth.lerp(ctx.partialTick(), entity.zOld, entity.getZ()) - visualizationContext.renderOrigin()
				.getZ());

		matrix.translation(x, y, z);
		matrix.rotate(ctx.camera()
				.rotation());
		matrix.scale(-0.025F, -0.025F, 0.025F);

		float h = (float) (-Minecraft.getInstance().font.width(displayName) / 2);

		matrix.translate(h, 0.0f, 0.0f);

		text.backgroundColor(j);
		text.setup(sequence, layers, matrix, LevelRenderer.getLightColor(level, entity.blockPosition()));
	}

	@Override
	protected void _delete() {
		text.delete();
	}
}
