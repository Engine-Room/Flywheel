package dev.engine_room.flywheel.lib.visual.component;

import org.joml.Quaternionf;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.LineModelBuilder;
import dev.engine_room.flywheel.lib.model.ModelHolder;
import dev.engine_room.flywheel.lib.visual.EntityComponent;
import dev.engine_room.flywheel.lib.visual.SmartRecycler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class HitboxComponent implements EntityComponent {
	//    010------110
	//    /|       /|
	//   / |      / |
	// 011------111 |
	//  |  |     |  |
	//  | 000----|-100
	//  | /      | /
	//  |/       |/
	// 001------101
	private static final ModelHolder BOX = new ModelHolder(() -> LineModelBuilder.withCapacity(12)
			// Starting from 0, 0, 0
			.line(0, 0, 0, 0, 0, 1)
			.line(0, 0, 0, 0, 1, 0)
			.line(0, 0, 0, 1, 0, 0)
			// Starting from 0, 1, 1
			.line(0, 1, 1, 0, 1, 0)
			.line(0, 1, 1, 0, 0, 1)
			.line(0, 1, 1, 1, 1, 1)
			// Starting from 1, 0, 1
			.line(1, 0, 1, 1, 0, 0)
			.line(1, 0, 1, 1, 1, 1)
			.line(1, 0, 1, 0, 0, 1)
			// Starting from 1, 1, 0
			.line(1, 1, 0, 1, 1, 1)
			.line(1, 1, 0, 1, 0, 0)
			.line(1, 1, 0, 0, 1, 0)
			.build());

	private static final ModelHolder LINE = new ModelHolder(() -> LineModelBuilder.withCapacity(1)
			.line(0, 0, 0, 0, 2, 0)
			.build());

	private final VisualizationContext context;
	private final Entity entity;

	private boolean showEyeBox;

	private final SmartRecycler<Model, TransformedInstance> recycler;

	public HitboxComponent(VisualizationContext context, Entity entity) {
		this.context = context;
		this.entity = entity;
		this.showEyeBox = entity instanceof LivingEntity;

		this.recycler = new SmartRecycler<>(this::createInstance);
	}

	private TransformedInstance createInstance(Model model) {
		TransformedInstance instance = context.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();
		instance.light(LightTexture.FULL_BLOCK);
		instance.setChanged();
		return instance;
	}

	public HitboxComponent showEyeBox(boolean renderEyeBox) {
		this.showEyeBox = renderEyeBox;
		return this;
	}

	@Override
	public void beginFrame(DynamicVisual.Context context) {
		recycler.resetCount();

		var shouldRenderHitBoxes = Minecraft.getInstance()
				.getEntityRenderDispatcher()
				.shouldRenderHitBoxes();
		if (shouldRenderHitBoxes && !entity.isInvisible() && !Minecraft.getInstance()
				.showOnlyReducedInfo()) {
			double entityX = Mth.lerp(context.partialTick(), entity.xOld, entity.getX());
			double entityY = Mth.lerp(context.partialTick(), entity.yOld, entity.getY());
			double entityZ = Mth.lerp(context.partialTick(), entity.zOld, entity.getZ());

			var bbWidth = entity.getBbWidth();
			var bbHeight = entity.getBbHeight();
			var bbWidthHalf = bbWidth * 0.5;
			recycler.get(BOX.get())
					.loadIdentity()
					.translate(entityX - bbWidthHalf, entityY, entityZ - bbWidthHalf)
					.scale(bbWidth, bbHeight, bbWidth)
					.setChanged();

			// TODO: multipart entities, but forge seems to have an
			//  injection for them so we'll need platform specific code.

			if (showEyeBox) {
				recycler.get(BOX.get())
						.loadIdentity()
						.translate(entityX - bbWidthHalf, entityY + entity.getEyeHeight() - 0.01, entityZ - bbWidthHalf)
						.scale(bbWidth, 0.02f, bbWidth)
						.color(255, 0, 0)
						.setChanged();
			}

			var viewVector = entity.getViewVector(context.partialTick());

			recycler.get(LINE.get())
					.loadIdentity()
					.translate(entityX, entityY + entity.getEyeHeight(), entityZ)
					.rotate(new Quaternionf().rotateTo(0, 1, 0, (float) viewVector.x, (float) viewVector.y, (float) viewVector.z))
					.color(0, 0, 255)
					.setChanged();
		}

		recycler.discardExtra();
	}

	@Override
	public void delete() {
		recycler.delete();
	}
}
