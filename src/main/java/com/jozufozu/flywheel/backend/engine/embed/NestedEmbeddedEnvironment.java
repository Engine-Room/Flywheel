package com.jozufozu.flywheel.backend.engine.embed;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.backend.engine.EngineImpl;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.world.level.BlockAndTintGetter;

public class NestedEmbeddedEnvironment extends AbstractEmbeddedEnvironment {
	private final AbstractEmbeddedEnvironment parent;

	public NestedEmbeddedEnvironment(AbstractEmbeddedEnvironment parent, EngineImpl engine, RenderStage renderStage) {
		super(engine, renderStage);
		this.parent = parent;
		parent.acquire();
	}

	@Override
	public void collectLight(BlockAndTintGetter level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
	}

	@Override
	public void invalidateLight() {
	}

	@Override
	public void setupLight(GlProgram program) {
		parent.setupLight(program);
	}

	@Override
	public void composeMatrices(Matrix4f pose, Matrix3f normal) {
		parent.composeMatrices(pose, normal);
		pose.mul(this.pose);
		normal.mul(this.normal);
	}

	@Override
	public void actuallyDelete() {
		parent.release();
	}
}
