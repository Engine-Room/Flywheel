package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.BitSet;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.attrib.VertexFormat;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.model.ModelAllocator;
import com.jozufozu.flywheel.backend.model.IBufferedModel;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.AttribUtil;

/**
 * An instancer is how you interact with an instanced model.
 * <p>
 *     Instanced models can have many copies, and on most systems it's very fast to draw all of the copies at once.
 *     There is no limit to how many copies an instanced model can have.
 *     Each copy is represented by an InstanceData object.
 * </p>
 * <p>
 *     When you call {@link #createInstance()} you are given an InstanceData object that you can manipulate however
 *     you want. The changes you make to the InstanceData object are automatically made visible, and persistent.
 *     Changing the position of your InstanceData object every frame means that that copy of the model will be in a
 *     different position in the world each frame. Setting the position of your InstanceData once and not touching it
 *     again means that your model will be in the same position in the world every frame. This persistence is useful
 *     because it means the properties of your model don't have to be re-evaluated every frame.
 * </p>
 *
 * @param <D> the data that represents a copy of the instanced model.
 */
public class GPUInstancer<D extends InstanceData> implements Instancer<D> {

	private final ModelAllocator modelAllocator;
	private final IModel modelData;
	private final VertexFormat instanceFormat;
	private final IInstanceFactory<D> factory;

	private IBufferedModel model;
	private GlVertexArray vao;
	private GlBuffer instanceVBO;
	private int glBufferSize = -1;
	private int glInstanceCount = 0;
	private boolean deleted;
	private boolean initialized;

	private final ArrayList<D> data = new ArrayList<>();

	boolean anyToRemove;
	boolean anyToUpdate;

	public GPUInstancer(ModelAllocator modelAllocator, IModel model, IInstanceFactory<D> factory, VertexFormat instanceFormat) {
		this.modelAllocator = modelAllocator;
		this.modelData = model;
		this.factory = factory;
		this.instanceFormat = instanceFormat;
	}

	/**
	 * @return a handle to a new copy of this model.
	 */
	@Override
	public D createInstance() {
		return _add(factory.create(this));
	}

	/**
	 * Copy a data from another Instancer to this.
	 *
	 * This has the effect of swapping out one model for another.
	 * @param inOther the data associated with a different model.
	 */
	@Override
	public void stealInstance(D inOther) {
		if (inOther.owner == this) return;

		inOther.delete();
		// sike, we want to keep it, changing the owner reference will still delete it in the other
		inOther.removed = false;
		_add(inOther);
	}

	public void render() {
		if (invalid()) return;

		vao.bind();
		renderSetup();

		if (glInstanceCount > 0) model.drawInstances(glInstanceCount);

		// persistent mapping sync point
		instanceVBO.doneForThisFrame();

		vao.unbind();
	}

	private boolean invalid() {
		return deleted || model == null;
	}

	public void init() {
		if (isInitialized()) return;

		initialized = true;

		vao = new GlVertexArray();

		model = modelAllocator.alloc(modelData, arenaModel -> {
			vao.bind();

			model.setupState();

			vao.unbind();
		});

		vao.bind();

		instanceVBO = GlBuffer.requestPersistent(GlBufferType.ARRAY_BUFFER);
		AttribUtil.enableArrays(model.getAttributeCount() + instanceFormat.getAttributeCount());

		vao.unbind();
	}

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isEmpty() {
		return !anyToUpdate && !anyToRemove && glInstanceCount == 0;
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		data.clear();
		anyToRemove = true;
	}

	/**
	 * Free acquired resources. All other Instancer methods are undefined behavior after calling delete.
	 */
	public void delete() {
		if (invalid()) return;

		deleted = true;

		model.delete();

		instanceVBO.delete();
		vao.delete();
	}

	private D _add(D instanceData) {
		instanceData.owner = this;

		instanceData.dirty = true;
		anyToUpdate = true;
		synchronized (data) {
			data.add(instanceData);
		}

		return instanceData;
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		instanceVBO.bind();
		if (!realloc()) {

			if (anyToRemove) {
				clearBufferTail();
			}

			if (anyToUpdate) {
				updateBuffer();
			}

			glInstanceCount = data.size();
		}

		instanceVBO.unbind();

		anyToRemove = anyToUpdate = false;
	}

	private void clearBufferTail() {
		int size = data.size();
		final int offset = size * instanceFormat.getStride();
		final int length = glBufferSize - offset;
		if (length > 0) {
			instanceVBO.getBuffer(offset, length)
					.putByteArray(new byte[length])
					.flush();
		}
	}

	private void updateBuffer() {
		final int size = data.size();

		if (size <= 0) return;

		final int stride = instanceFormat.getStride();
		final BitSet dirtySet = getDirtyBitSet();

		if (dirtySet.isEmpty()) return;

		final int firstDirty = dirtySet.nextSetBit(0);
		final int lastDirty = dirtySet.previousSetBit(size);

		final int offset = firstDirty * stride;
		final int length = (1 + lastDirty - firstDirty) * stride;

		if (length > 0) {
			MappedBuffer mapped = instanceVBO.getBuffer(offset, length);

			dirtySet.stream()
					.forEach(i -> {
						final D d = data.get(i);

						mapped.position(i * stride);
						d.write(mapped);
					});
			mapped.flush();
		}
	}

	private BitSet getDirtyBitSet() {
		final int size = data.size();
		final BitSet dirtySet = new BitSet(size);

		for (int i = 0; i < size; i++) {
			D element = data.get(i);
			if (element.dirty) {
				dirtySet.set(i);

				element.dirty = false;
			}
		}
		return dirtySet;
	}

	private boolean realloc() {
		int size = this.data.size();
		int stride = instanceFormat.getStride();
		int requiredSize = size * stride;
		if (requiredSize > glBufferSize) {
			glBufferSize = requiredSize + stride * 16;
			instanceVBO.alloc(glBufferSize);

			MappedBuffer buffer = instanceVBO.getBuffer(0, glBufferSize);
			for (D datum : data) {
				datum.write(buffer);
			}
			buffer.flush();

			glInstanceCount = size;

			informAttribDivisors();

			return true;
		}
		return false;
	}

	private void removeDeletedInstances() {
		// Figure out which elements are to be removed.
		final int oldSize = this.data.size();
		int removeCount = 0;
		final BitSet removeSet = new BitSet(oldSize);
		for (int i = 0; i < oldSize; i++) {
			final D element = this.data.get(i);
			if (element.removed || element.owner != this) {
				removeSet.set(i);
				removeCount++;
			}
		}

		final int newSize = oldSize - removeCount;

		// shift surviving elements left over the spaces left by removed elements
		for (int i = 0, j = 0; (i < oldSize) && (j < newSize); i++, j++) {
			i = removeSet.nextClearBit(i);

			if (i != j) {
				D element = data.get(i);
				data.set(j, element);
				element.dirty = true;
			}
		}

		anyToUpdate = true;

		data.subList(newSize, oldSize)
				.clear();

	}

	private void informAttribDivisors() {
		int staticAttributes = model.getAttributeCount();
		instanceFormat.vertexAttribPointers(staticAttributes);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			Backend.getInstance().compat.instancedArrays.vertexAttribDivisor(i + staticAttributes, 1);
		}
	}

	@Override
	public void markDirty(InstanceData instanceData) {
		anyToUpdate = true;
		instanceData.dirty = true;
	}

	@Override
	public void markRemoval(InstanceData instanceData) {
		anyToRemove = true;
		instanceData.removed = true;
	}
}
