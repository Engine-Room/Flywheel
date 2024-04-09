package com.jozufozu.flywheel.vanilla;

import java.util.List;

import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.vanilla.model.InstanceTree;
import com.jozufozu.flywheel.vanilla.model.MeshTreeCache;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.util.Mth;

public class QuadrupedComponent extends AgeableListComponent {
	public final InstanceTree root;

	public final InstanceTree head;
	public final InstanceTree body;
	public final InstanceTree rightHindLeg;
	public final InstanceTree leftHindLeg;
	public final InstanceTree rightFrontLeg;
	public final InstanceTree leftFrontLeg;

	public QuadrupedComponent(InstancerProvider instancerProvider, ModelLayerLocation layer, Material material, Config config) {
		super(config);

		var meshTree = MeshTreeCache.get(layer);

		this.root = InstanceTree.create(instancerProvider, meshTree, material);

		this.head = root.child("head");
		this.body = root.child("body");
		this.rightHindLeg = root.child("right_hind_leg");
		this.leftHindLeg = root.child("left_hind_leg");
		this.rightFrontLeg = root.child("right_front_leg");
		this.leftFrontLeg = root.child("left_front_leg");
	}

	public void setupAnim(float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
		this.head.xRot = pHeadPitch * ((float) Math.PI / 180F);
		this.head.yRot = pNetHeadYaw * ((float) Math.PI / 180F);
		this.rightHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
		this.leftHindLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
		this.rightFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + (float) Math.PI) * 1.4F * pLimbSwingAmount;
		this.leftFrontLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
	}

	public void delete() {
		root.delete();
	}

	@Override
	protected Iterable<InstanceTree> headParts() {
		return List.of(head);
	}

	@Override
	protected Iterable<InstanceTree> bodyParts() {
		return List.of(body, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg);
	}
}
