package com.jozufozu.flywheel.backend.model;

import org.lwjgl.opengl.GL20;
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

/**
 * An indexed triangle model. Just what the driver ordered.
 *
 * <br><em>This should be favored over a normal BufferedModel.</em>
 */
public class IndexedModel implements BufferedModel {

	protected final VertexType type;
	protected final Model model;
	protected final GlPrimitive primitiveMode;
	protected ElementBuffer ebo;
	protected GlBuffer vbo;
	protected boolean deleted;

	public IndexedModel(Model model) {
		this(model, model.getType());
	}

	public IndexedModel(Model model, VertexType type) {
		this.type = type;
		this.model = model;
		this.primitiveMode = GlPrimitive.TRIANGLES;

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.ensureCapacity(type.byteOffset(model.vertexCount()));

		// mirror it in system memory, so we can write to it, and upload our model.
		try (MappedBuffer buffer = vbo.getBuffer()) {
			type.createWriter(buffer.unwrap())
					.writeVertexList(model.getReader());
		} catch (Exception e) {
			Flywheel.LOGGER.error(String.format("Error uploading model '%s':", model.name()), e);
		}

		vbo.unbind();

		this.ebo = model.createEBO();
	}

	/**
	 * The VBO/VAO should be bound externally.
	 */
	public void setupState(GlVertexArray vao) {
		// XXX ARRAY_BUFFER is bound and not reset or restored
		vbo.bind();
		vao.enableArrays(getAttributeCount());
		vao.bindAttributes(0, getType().getLayout());
		ebo.bind();
	}

	@Override
	public void drawCall() {
		GL20.glDrawElements(primitiveMode.glEnum, ebo.getElementCount(), ebo.getEboIndexType().asGLType, 0);
	}

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	@Override
	public void drawInstances(int instanceCount) {
		if (!valid()) return;

		GL31.glDrawElementsInstanced(primitiveMode.glEnum, ebo.getElementCount(), ebo.getEboIndexType().asGLType, 0, instanceCount);
	}

	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public VertexType getType() {
		return type;
	}

	public int getVertexCount() {
		return model.vertexCount();
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}
}
