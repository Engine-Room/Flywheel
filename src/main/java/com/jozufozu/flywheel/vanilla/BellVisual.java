package com.jozufozu.flywheel.vanilla;

import java.util.List;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.event.RenderStage;
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

	private float lastRingTime = Float.NaN;

	public BellVisual(VisualizationContext ctx, BellBlockEntity blockEntity) {
		super(ctx, blockEntity);
	}

	@Override
	public void init(float partialTick) {
		bell = createBellInstance().setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getVisualPosition());

		updateRotation(partialTick);

		super.init(partialTick);
	}

	private OrientedInstance createBellInstance() {
		return instancerProvider.instancer(InstanceTypes.ORIENTED, BELL_MODEL.get(), RenderStage.AFTER_BLOCK_ENTITIES)
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
		float ringTime = (float) blockEntity.ticks + partialTick;

		if (ringTime == lastRingTime) {
			return;
		}
		lastRingTime = ringTime;

		if (blockEntity.shaking) {
			float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

			Vector3f ringAxis = blockEntity.clickDirection.getCounterClockWise()
					.step();

			bell.setRotation(new Quaternionf(new AxisAngle4f(angle, ringAxis)));
		} else {
			bell.setRotation(new Quaternionf());
		}
	}

	@Override
	public void updateLight() {
		relight(pos, bell);
	}

	@Override
	public List<Instance> getCrumblingInstances() {
		return List.of(bell);
	}

	@Override
	protected void _delete() {
		bell.delete();
	}
}
