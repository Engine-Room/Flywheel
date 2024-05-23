package dev.engine_room.flywheel.lib.internal;

import org.slf4j.Logger;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;

public interface FlwLibLink {
	FlwLibLink INSTANCE = DependencyInjection.load(FlwLibLink.class, "dev.engine_room.flywheel.impl.FlwLibLinkImpl");

	Logger getLogger();

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);
}
