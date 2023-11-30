package com.jozufozu.flywheel.backend.engine.indirect;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.engine.AbstractEngine;
import com.jozufozu.flywheel.backend.engine.AbstractInstancer;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class IndirectEngine extends AbstractEngine {
	private final IndirectDrawManager drawManager = new IndirectDrawManager();
	private final Flag flushFlag = new NamedFlag("flushed");

	public IndirectEngine(int maxOriginDistance) {
		super(maxOriginDistance);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SyncedPlan.of(this::flushDrawManager);
	}

	private void flushDrawManager() {
		try (var state = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
		flushFlag.raise();
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		executor.syncUntil(flushFlag::isRaised);
		if (stage.isLast()) {
			flushFlag.lower();
		}

        if (!drawManager.hasStage(stage)) {
            return;
        }

        try (var restoreState = GlStateTracker.getRestoreState()) {
            setup();

            for (var list : drawManager.renderLists.values()) {
                list.submit(stage);
            }
        }
    }

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		// TODO: implement
	}

	private void setup() {
		GlTextureUnit.T2.makeActive();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL32.GL_LEQUAL);
		RenderSystem.enableCull();
	}

	@Override
	protected InstancerStorage<? extends AbstractInstancer<?>> getStorage() {
		return drawManager;
	}

	@Override
	public void delete() {
		drawManager.invalidate();
	}
}
