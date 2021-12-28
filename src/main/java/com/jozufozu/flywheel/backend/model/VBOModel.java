package com.jozufozu.flywheel.backend.model;

import static org.lwjgl.opengl.GL11.glDrawArrays;

import org.lwjgl.opengl.GL31;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedGlBuffer;
import com.jozufozu.flywheel.core.model.Model;

public class VBOModel implements BufferedModel {

	protected final Model model;
	protected final GlPrimitive primitiveMode;
	protected GlBuffer vbo;
	protected boolean deleted;

	public VBOModel(GlPrimitive primitiveMode, Model model) {
		this.model = model;
		this.primitiveMode = primitiveMode;

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.ensureCapacity(model.size());

		// mirror it in system memory, so we can write to it, and upload our model.
		try (MappedBuffer buffer = vbo.getBuffer()) {
			model.writeInto(buffer.unwrap());
		} catch (Exception e) {
			Flywheel.LOGGER.error(String.format("Error uploading model '%s':", model.name()), e);
		}

		vbo.unbind();
	}

	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public VertexType getType() {
		return model.getType();
	}

	public int getVertexCount() {
		return model.vertexCount();
	}

	/**
	 * The VBO/VAO should be bound externally.
	 */
	public void setupState(GlVertexArray vao) {
		vbo.bind();
		vao.enableArrays(getAttributeCount());
		vao.bindAttributes(0, getLayout());
	}

	public void drawCall() {
		glDrawArrays(primitiveMode.glEnum, 0, getVertexCount());
	}

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	public void drawInstances(int instanceCount) {
		if (!valid()) return;

		GL31.glDrawArraysInstanced(primitiveMode.glEnum, 0, getVertexCount(), instanceCount);
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}
}

