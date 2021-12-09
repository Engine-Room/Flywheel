package com.jozufozu.flywheel.backend.model;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL31;

import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.core.model.Model;

/**
 * An indexed triangle model. Just what the driver ordered.
 *
 * <br><em>This should be favored over a normal BufferedModel.</em>
 */
public class IndexedModel extends BufferedModel {

	protected ElementBuffer ebo;

	public IndexedModel(Model model) {
		super(GlPrimitive.TRIANGLES, model);

		this.ebo = model.createEBO();
	}

	@Override
	public void setupState() {
		super.setupState();
		ebo.bind();
	}

	@Override
	public void clearState() {
		super.clearState();
		ebo.unbind();
	}

	@Override
	public void drawCall() {
		ebo.bind();
		GL20.glDrawElements(primitiveMode.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0);
	}

	@Override
	public void drawInstances(int instanceCount) {
		if (!valid()) return;

		GL31.glDrawElementsInstanced(primitiveMode.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0, instanceCount);
	}
}
