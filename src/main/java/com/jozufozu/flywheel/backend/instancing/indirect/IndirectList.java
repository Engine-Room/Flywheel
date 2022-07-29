package com.jozufozu.flywheel.backend.instancing.indirect;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GL46C;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.instancing.instancing.MeshPool;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.model.Mesh;

public class IndirectList<T extends InstancedPart> {

	final GlProgram compute;
	final GlProgram draw;

	/**
	 * Stores raw instance data per-object.
	 */
	DSABuffer objectBuffer;

	/**
	 * Stores bounding spheres
	 */
	DSABuffer boundingSphereBuffer;

	/**
	 * Stores drawIndirect structs.
	 */
	DSABuffer drawBuffer;
	DSABuffer targetBuffer;

	final int[] buffers = new int[4];

	final List<Batch<T>> batches = new ArrayList<>();

	IndirectList(StructType<T> structType) {
		GL46.glCreateBuffers(buffers);
		objectBuffer = new DSABuffer(buffers[0]);
		targetBuffer = new DSABuffer(buffers[1]);
		boundingSphereBuffer = new DSABuffer(buffers[2]);
		drawBuffer = new DSABuffer(buffers[3]);

		compute = ComputeCompiler.INSTANCE.get(Components.Files.CULL_INSTANCES);
		draw = null;
	}

	public void add(Mesh mesh, IndirectInstancer<T> instancer) {
		var pool = MeshPool.getInstance();
		var buffered = pool.alloc(mesh);

		batches.add(new Batch<>(instancer, buffered));
	}

	public void prepare() {

		try (var stack = MemoryStack.stackPush()) {
			var size = batches.size() * 20;
			long basePtr = stack.nmalloc(size);
			long writePtr = basePtr;
			for (Batch<T> batch : batches) {
				batch.writeIndirectCommand(writePtr);
				writePtr += 20;
			}
			GL46C.nglNamedBufferData(drawBuffer.id, size, basePtr, GL46.GL_STREAM_DRAW);
		}
	}

	public void submit() {

		compute.bind();
		GL46.glBindBuffersBase(GL46.GL_SHADER_STORAGE_BUFFER, 0, buffers);

		var groupCount = (getTotalInstanceCount() + 31) >> 5; // ceil(totalInstanceCount / 32)
		GL46.glDispatchCompute(groupCount, 1, 1);

		draw.bind();
		GL46.glMemoryBarrier(GL46.GL_SHADER_STORAGE_BARRIER_BIT);
		GL46.glBindBuffer(GL46.GL_DRAW_INDIRECT_BUFFER, drawBuffer.id);

		GL46.glMultiDrawElementsIndirect(GL46.GL_TRIANGLES, GL46.GL_UNSIGNED_INT, 0, batches.size(), 0);
	}

	private int getTotalInstanceCount() {
		return 0;
	}

	private static final class Batch<T extends InstancedPart> {
		final IndirectInstancer<T> instancer;
		final MeshPool.BufferedMesh mesh;

		private Batch(IndirectInstancer<T> instancer, MeshPool.BufferedMesh mesh) {
			this.instancer = instancer;
			this.mesh = mesh;
		}

		public void writeIndirectCommand(long ptr) {
			// typedef  struct {
			//    GLuint  count;
			//    GLuint  instanceCount;
			//    GLuint  firstIndex;
			//    GLuint  baseVertex;
			//    GLuint  baseInstance;
			//} DrawElementsIndirectCommand;

			MemoryUtil.memPutInt(ptr, mesh.getVertexCount());
			MemoryUtil.memPutInt(ptr + 4, 0);
			MemoryUtil.memPutInt(ptr + 8, 0);
			MemoryUtil.memPutInt(ptr + 12, mesh.getBaseVertex());
			MemoryUtil.memPutInt(ptr + 16, 0);
		}
	}
}
