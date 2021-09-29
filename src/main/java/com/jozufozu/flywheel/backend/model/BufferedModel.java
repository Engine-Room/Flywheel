package com.jozufozu.flywheel.backend.model;

import static org.lwjgl.opengl.GL20.glDrawArrays;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlPrimitive;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.MappedGlBuffer;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.AttribUtil;

public class BufferedModel implements IBufferedModel {

	protected final Model model;
	protected final GlPrimitive primitiveMode;
	protected GlBuffer vbo;
	protected boolean deleted;

	public BufferedModel(GlPrimitive primitiveMode, Model model) {
		this.model = model;
		this.primitiveMode = primitiveMode;

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.alloc(model.size());

		// mirror it in system memory so we can write to it, and upload our model.
		MappedBuffer buffer = vbo.getBuffer(0, model.size());
		model.buffer(buffer);
		buffer.flush();

		vbo.unbind();
	}

	public boolean isDeleted() {
		return deleted;
	}

	public VertexFormat getFormat() {
		return model.format();
	}

	public int getVertexCount() {
		return model.vertexCount();
	}

	/**
	 * The VBO/VAO should be bound externally.
	 */
	public void setupState() {
		vbo.bind();
		AttribUtil.enableArrays(getAttributeCount());
		getFormat().vertexAttribPointers(0);
	}

	public void clearState() {
		AttribUtil.disableArrays(getAttributeCount());
		vbo.unbind();
	}

	public void drawCall() {
		glDrawArrays(primitiveMode.glEnum, 0, getVertexCount());
	}

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	public void drawInstances(int instanceCount) {
		if (!valid()) return;

		Backend.getInstance().compat.drawInstanced.drawArraysInstanced(primitiveMode, 0, getVertexCount(), instanceCount);
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}
}

