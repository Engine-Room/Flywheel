package com.jozufozu.flywheel.backend.engine.batching;

import com.mojang.blaze3d.vertex.PoseStack;

public record FrameContext(net.minecraft.client.multiplayer.ClientLevel level, PoseStack.Pose matrices,
						   org.joml.FrustumIntersection frustum) {
}
