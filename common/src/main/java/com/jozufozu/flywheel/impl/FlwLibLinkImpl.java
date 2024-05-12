package com.jozufozu.flywheel.impl;

import org.slf4j.Logger;

import com.jozufozu.flywheel.impl.extension.PoseStackExtension;
import com.jozufozu.flywheel.lib.internal.FlwLibLink;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

public class FlwLibLinkImpl implements FlwLibLink {
	@Override
	public Logger getLogger() {
		return FlwImpl.LOGGER;
	}

	@Override
	public PoseTransformStack getPoseTransformStackOf(PoseStack stack) {
		return ((PoseStackExtension) stack).flywheel$transformStack();
	}
}
