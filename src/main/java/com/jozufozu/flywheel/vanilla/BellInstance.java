package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;
import com.jozufozu.flywheel.backend.model.BufferedModel;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.core.model.ModelPart;
import com.jozufozu.flywheel.util.AnimationTickHolder;

import net.minecraft.client.renderer.tileentity.BellTileEntityRenderer;
import net.minecraft.tileentity.BellTileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public class BellInstance extends TileEntityInstance<BellTileEntity> implements IDynamicInstance {

	private final OrientedData bell;

	public BellInstance(MaterialManager<?> materialManager, BellTileEntity tile) {
		super(materialManager, tile);

		bell = createBellInstance()
				.setPivot(0.5f, 0.75f, 0.5f)
				.setPosition(getInstancePosition());
	}

	@Override
	public void beginFrame() {
		if (tile.isRinging) {
			float ringness = (float)tile.ringingTicks + AnimationTickHolder.getPartialTicks();
			float angle = MathHelper.sin(ringness / (float)Math.PI) / (4.0F + ringness / 3.0F);

			Vector3f ringAxis = tile.ringDirection.rotateYCCW().getUnitVector();

			bell.setRotation(ringAxis.getRadialQuaternion(angle));
		}
	}

	@Override
	public void updateLight() {
		relight(getWorldPosition(), bell);
	}

	@Override
	public void remove() {
		bell.delete();
	}

	private OrientedData createBellInstance() {
		return materialManager.getMaterial(Materials.ORIENTED)
				.get(tile.getType(), BellInstance::createBellModel)
				.createInstance();
	}

	private static BufferedModel createBellModel() {
		return ModelPart.builder(32, 32)
				.sprite(BellTileEntityRenderer.field_217653_c.getSprite())
				.cuboid()
				.start(5.0F, 6.0F, 5.0F)
				.size(6.0F, 7.0F, 6.0F)
				.endCuboid()
				.cuboid()
				.textureOffset(0, 13)
				.start(4.0F, 4.0F, 4.0F)
				.size(8.0F, 2.0F, 8.0F)
				.endCuboid()
				.build();
	}
}
