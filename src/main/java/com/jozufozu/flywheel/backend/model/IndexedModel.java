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
import com.jozufozu.flywheel.core.model.Mesh;

/**
 * An indexed triangle model. Just what the driver ordered.
 *
 * <br><em>This should be favored over a normal BufferedModel.</em>
 */
public class IndexedModel implements BufferedModel {

	protected final Mesh mesh;
	protected final GlPrimitive primitiveMode;
	protected ElementBuffer ebo;
	protected GlBuffer vbo;
	protected boolean deleted;

	public IndexedModel(Mesh mesh) {
		this.mesh = mesh;
		this.primitiveMode = GlPrimitive.TRIANGLES;

		vbo = new MappedGlBuffer(GlBufferType.ARRAY_BUFFER);

		vbo.bind();
		// allocate the buffer on the gpu
		vbo.ensureCapacity(mesh.size());

		// mirror it in system memory, so we can write to it, and upload our model.
		try (MappedBuffer buffer = vbo.getBuffer()) {
			mesh.writeInto(buffer.unwrap());
		} catch (Exception e) {
			Flywheel.LOGGER.error(String.format("Error uploading model '%s':", mesh.name()), e);
		}

		vbo.unbind();

		this.ebo = mesh.createEBO();
	}

	/**
	 * The VBO/VAO should be bound externally.
	 */
	public void setupState(GlVertexArray vao) {
		vbo.bind();
		vao.enableArrays(getAttributeCount());
		vao.bindAttributes(0, getType().getLayout());
	}

	@Override
	public void drawCall() {
		ebo.bind();
		GL20.glDrawElements(primitiveMode.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0);
	}

	/**
	 * Draws many instances of this model, assuming the appropriate state is already bound.
	 */
	@Override
	public void drawInstances(int instanceCount) {
		if (!valid()) return;

		ebo.bind();

		GL31.glDrawElementsInstanced(primitiveMode.glEnum, ebo.elementCount, ebo.eboIndexType.getGlEnum(), 0, instanceCount);
	}

	public boolean isDeleted() {
		return deleted;
	}

	@Override
	public VertexType getType() {
		return mesh.getType();
	}

	public int getVertexCount() {
		return mesh.vertexCount();
	}

	public void delete() {
		if (deleted) return;

		deleted = true;
		vbo.delete();
	}
}
