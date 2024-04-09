package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.vanilla.model.InstanceTree;
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
			for (InstanceTree p_102081_ : this.headParts()) {
				p_102081_.render(pPoseStack);
			}
			pPoseStack.popPose();
			pPoseStack.pushPose();
			float f1 = 1.0F / this.config.babyBodyScale;
			pPoseStack.scale(f1, f1, f1);
			pPoseStack.translate(0.0F, this.config.bodyYOffset / 16.0F, 0.0F);
			for (InstanceTree p_102071_ : this.bodyParts()) {
				p_102071_.render(pPoseStack);
			}
			pPoseStack.popPose();
		} else {
			for (InstanceTree p_102061_ : this.headParts()) {
				p_102061_.render(pPoseStack);
			}
			for (InstanceTree p_102051_ : this.bodyParts()) {
				p_102051_.render(pPoseStack);
			}
		}

	}

	protected abstract Iterable<InstanceTree> headParts();

	protected abstract Iterable<InstanceTree> bodyParts();

	public record Config(boolean scaleHead, float babyYHeadOffset, float babyZHeadOffset, float babyHeadScale,
						 float babyBodyScale, float bodyYOffset) {
	}
}
