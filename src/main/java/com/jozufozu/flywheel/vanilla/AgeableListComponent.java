package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.lib.model.part.InstanceTree;
import com.mojang.blaze3d.vertex.PoseStack;

public abstract class AgeableListComponent {
	public float attackTime;
	public boolean riding;
	public boolean young = true;
	protected final Config config;

	public AgeableListComponent(Config config) {
		this.config = config;
	}

	public void updateInstances(PoseStack pPoseStack) {
		if (this.young) {
			pPoseStack.pushPose();
			if (this.config.scaleHead) {
				float f = 1.5F / this.config.babyHeadScale;
				pPoseStack.scale(f, f, f);
			}

			pPoseStack.translate(0.0F, this.config.babyYHeadOffset / 16.0F, this.config.babyZHeadOffset / 16.0F);
			for (InstanceTree headPart : this.headParts()) {
				headPart.updateInstances(pPoseStack);
			}
			pPoseStack.popPose();
			pPoseStack.pushPose();
			float f1 = 1.0F / this.config.babyBodyScale;
			pPoseStack.scale(f1, f1, f1);
			pPoseStack.translate(0.0F, this.config.bodyYOffset / 16.0F, 0.0F);
			for (InstanceTree bodyPart : this.bodyParts()) {
				bodyPart.updateInstances(pPoseStack);
			}
			pPoseStack.popPose();
		} else {
			for (InstanceTree headPart : this.headParts()) {
				headPart.updateInstances(pPoseStack);
			}
			for (InstanceTree bodyPart : this.bodyParts()) {
				bodyPart.updateInstances(pPoseStack);
			}
		}

	}

	protected abstract Iterable<InstanceTree> headParts();

	protected abstract Iterable<InstanceTree> bodyParts();

	public record Config(boolean scaleHead, float babyYHeadOffset, float babyZHeadOffset, float babyHeadScale,
						 float babyBodyScale, float bodyYOffset) {
	}
}
