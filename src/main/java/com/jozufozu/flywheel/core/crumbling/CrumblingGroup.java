package com.jozufozu.flywheel.core.crumbling;

import org.joml.Matrix4f;

import com.jozufozu.flywheel.backend.RenderLayer;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancedMaterialGroup;
import com.jozufozu.flywheel.backend.instancing.instancing.InstancingEngine;
import com.jozufozu.flywheel.core.shader.WorldProgram;
import com.jozufozu.flywheel.util.Textures;

import net.minecraft.client.renderer.RenderType;

public class CrumblingGroup<P extends WorldProgram> extends InstancedMaterialGroup<P> {

	public CrumblingGroup(InstancingEngine<P> owner, RenderType type) {
		super(owner, type);
	}

	// XXX See notes of overriden method
	@Override
	public void render(Matrix4f viewProjection, double camX, double camY, double camZ, RenderLayer layer) {
		CrumblingRenderer._currentLayer.setupRenderState();
		Textures.bindActiveTextures();
		renderAll(viewProjection, camX, camY, camZ, layer);
		CrumblingRenderer._currentLayer.clearRenderState();
	}
}
