package dev.engine_room.flywheel.impl;

import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.impl.extension.PoseStackExtension;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;

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
