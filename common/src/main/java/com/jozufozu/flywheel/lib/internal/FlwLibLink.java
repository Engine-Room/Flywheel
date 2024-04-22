package com.jozufozu.flywheel.lib.internal;

import com.jozufozu.flywheel.api.internal.DependencyInjection;
import com.jozufozu.flywheel.lib.transform.PoseTransformStack;
import com.mojang.blaze3d.vertex.PoseStack;

public interface FlwLibLink {
	FlwLibLink INSTANCE = DependencyInjection.load(FlwLibLink.class, "com.jozufozu.flywheel.impl.FlwLibLinkImpl");

	PoseTransformStack getPoseTransformStackOf(PoseStack stack);
}
