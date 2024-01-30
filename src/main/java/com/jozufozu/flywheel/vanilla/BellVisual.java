package com.jozufozu.flywheel.vanilla;

import java.util.function.Consumer;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.OrientedInstance;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.ModelHolder;
import com.jozufozu.flywheel.lib.model.SingleMeshModel;
import com.jozufozu.flywheel.lib.model.part.ModelPartConverter;
import com.jozufozu.flywheel.lib.visual.AbstractBlockEntityVisual;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellVisual extends AbstractBlockEntityVisual<BellBlockEntity> implements DynamicVisual {
	private static final ModelHolder BELL_MODEL = new ModelHolder(() -> {
		return new SingleMeshModel(ModelPartConverter.convert(ModelLayers.BELL, BellRenderer.BELL_RESOURCE_LOCATION.sprite(), "bell_body"), Materials.BELL);
	});

	private OrientedInstance bell;

	private boolean wasShaking = false;

	public BellVisual(VisualizationContext ctx, BellBlockEntity blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init(float partialTick) {
		bell = createBellInstance().setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getVisualPosition());

		bell.setChanged();

		updateRotation(partialTick);

		super.init(partialTick);
	}

	private OrientedInstance createBellInstance() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BELL_MODEL.get())
				.createInstance();
	}

	@Override
	public void beginFrame(VisualFrameContext context) {
		if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
			return;
		}

		updateRotation(context.partialTick());
	}

	private void updateRotation(float partialTick) {
		if (blockEntity.shaking) {
			float ringTime = (float) blockEntity.ticks + partialTick;
			float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

			Vector3f ringAxis = blockEntity.clickDirection.getCounterClockWise()
					.step();

			bell.setRotation(new Quaternionf(new AxisAngle4f(angle, ringAxis)))
					.setChanged();

			wasShaking = true;
		} else if (wasShaking) {
			bell.setRotation(new Quaternionf())
					.setChanged();

			wasShaking = false;
		}
	}

	@Override
	public void updateLight() {
		relight(pos, bell);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(bell);
	}

	@Override
	protected void _delete() {
		bell.delete();
	}
}
