package com.jozufozu.flywheel.lib.model.buffering;

import java.util.Random;

import com.jozufozu.flywheel.lib.model.buffering.ModelBufferingUtil.BufferWrapper;
import com.jozufozu.flywheel.lib.model.buffering.ModelBufferingUtil.ShadeSeparatingBufferWrapper;
import com.mojang.blaze3d.vertex.PoseStack;

public class ModelBufferingObjects {
	public static final ThreadLocal<ModelBufferingObjects> THREAD_LOCAL = ThreadLocal.withInitial(ModelBufferingObjects::new);

	public final PoseStack identityPoseStack = new PoseStack();
	@SuppressWarnings("rawtypes")
	public final BufferWrapper bufferWrapper = new BufferWrapper<>();
	@SuppressWarnings("rawtypes")
	public final ShadeSeparatingBufferWrapper shadeSeparatingBufferWrapper = new ShadeSeparatingBufferWrapper<>();
	public final Random random = new Random();
}
