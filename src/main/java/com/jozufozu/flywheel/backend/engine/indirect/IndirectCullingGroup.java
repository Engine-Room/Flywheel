package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glUniform1ui;
import static org.lwjgl.opengl.GL42.GL_COMMAND_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.glDispatchCompute;
import static org.lwjgl.opengl.GL43.glMultiDrawElementsIndirect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.Contexts;

public class IndirectCullingGroup<I extends Instance> {
	private static final Comparator<IndirectDraw> DRAW_COMPARATOR = Comparator.comparing(IndirectDraw::stage)
			.thenComparing(IndirectDraw::material, MaterialRenderState.COMPARATOR);

	private static final int DRAW_BARRIER_BITS = GL_SHADER_STORAGE_BARRIER_BIT | GL_COMMAND_BARRIER_BIT;

	private final GlProgram cullProgram;
	private final GlProgram applyProgram;
	private final GlProgram drawProgram;

	private final long objectStride;
	private final IndirectBuffers buffers;
	private final IndirectMeshPool meshPool;
	private final List<IndirectModel> indirectModels = new ArrayList<>();
	private final List<IndirectDraw> indirectDraws = new ArrayList<>();
	private final Map<RenderStage, List<MultiDraw>> multiDraws = new EnumMap<>(RenderStage.class);
	private boolean needsDrawBarrier;
	private boolean needsSortDraws;
	private int instanceCountThisFrame;

	IndirectCullingGroup(InstanceType<I> instanceType) {
		var programs = IndirectPrograms.get();
		cullProgram = programs.getCullingProgram(instanceType);
		applyProgram = programs.getApplyProgram();
		drawProgram = programs.getIndirectProgram(instanceType, Contexts.DEFAULT);

		objectStride = instanceType.getLayout()
				.getStride() + IndirectBuffers.INT_SIZE;

		buffers = new IndirectBuffers(objectStride);

		meshPool = new IndirectMeshPool();
	}

	public void flush(StagingBuffer stagingBuffer) {
		needsDrawBarrier = true;
		instanceCountThisFrame = prepareModels();

		if (nothingToDo()) {
			return;
		}

		buffers.updateCounts(instanceCountThisFrame, indirectModels.size(), indirectDraws.size());

		if (needsSortDraws) {
			sortDraws();
			needsSortDraws = false;
		}

		meshPool.flush(stagingBuffer);
		uploadObjects(stagingBuffer);
		uploadModels(stagingBuffer);
		uploadDraws(stagingBuffer);
	}

	public void dispatchCull() {
		if (nothingToDo()) {
			return;
		}

		UniformBuffer.get().sync();
		cullProgram.bind();
		buffers.bindForCompute();
		glDispatchCompute(GlCompat.getComputeGroupCount(instanceCountThisFrame), 1, 1);
	}

	public void dispatchApply() {
		if (nothingToDo()) {
			return;
		}

		applyProgram.bind();
		buffers.bindForCompute();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		glDispatchCompute(GlCompat.getComputeGroupCount(indirectDraws.size()), 1, 1);
	}

	private boolean nothingToDo() {
		return indirectDraws.isEmpty() || instanceCountThisFrame == 0;
	}

	private boolean nothingToDo(RenderStage stage) {
		return nothingToDo() || !multiDraws.containsKey(stage);
	}

	/**
	 * @return the total instance count
	 */
	private int prepareModels() {
		int baseInstance = 0;
		for (var model : indirectModels) {
			model.prepare(baseInstance);
			baseInstance += model.instancer.getInstanceCount();
		}
		return baseInstance;
	}

	private void sortDraws() {
		multiDraws.clear();
		// sort by stage, then material
		indirectDraws.sort(DRAW_COMPARATOR);

		for (int start = 0, i = 0; i < indirectDraws.size(); i++) {
			var draw1 = indirectDraws.get(i);
			var material1 = draw1.material();
			var stage1 = draw1.stage();

			// if the next draw call has a different RenderStage or Material, start a new MultiDraw
			if (i == indirectDraws.size() - 1 || stage1 != indirectDraws.get(i + 1)
					.stage() || !material1.equals(indirectDraws.get(i + 1)
					.material())) {
				multiDraws.computeIfAbsent(stage1, s -> new ArrayList<>())
						.add(new MultiDraw(material1, start, i + 1));
				start = i + 1;
			}
		}
	}

	public boolean hasStage(RenderStage stage) {
		return multiDraws.containsKey(stage);
	}

	public void add(IndirectInstancer<I> instancer, Model model, RenderStage stage) {
		int modelIndex = indirectModels.size();
		instancer.setModelIndex(modelIndex);
		var indirectModel = new IndirectModel(instancer, modelIndex, model.boundingSphere());
		indirectModels.add(indirectModel);

		for (Map.Entry<Material, Mesh> entry : model.meshes().entrySet()) {
			IndirectMeshPool.BufferedMesh bufferedMesh = meshPool.alloc(entry.getValue());
			indirectDraws.add(new IndirectDraw(indirectModel, entry.getKey(), bufferedMesh, stage));
		}

		needsSortDraws = true;
	}

	public void submit(RenderStage stage) {
		if (nothingToDo(stage)) {
			return;
		}

		UniformBuffer.get().sync();
		drawProgram.bind();
		meshPool.bindForDraw();
		buffers.bindForDraw();

		drawBarrier();

		var flwBaseDraw = drawProgram.getUniformLocation("_flw_baseDraw");

		for (var multiDraw : multiDraws.get(stage)) {
			glUniform1ui(flwBaseDraw, multiDraw.start);
			multiDraw.submit();
		}
	}

	private void drawBarrier() {
		if (needsDrawBarrier) {
			glMemoryBarrier(DRAW_BARRIER_BITS);
			needsDrawBarrier = false;
		}
	}

	private void uploadObjects(StagingBuffer stagingBuffer) {
		long pos = 0;
		for (IndirectModel batch : indirectModels) {
			var instanceCount = batch.instancer.getInstanceCount();
			batch.writeObjects(stagingBuffer, pos, buffers.object.handle());

			pos += instanceCount * objectStride;
		}
	}

	private void uploadModels(StagingBuffer stagingBuffer) {
		var totalSize = indirectModels.size() * IndirectBuffers.MODEL_STRIDE;
		var handle = buffers.model.handle();

		stagingBuffer.enqueueCopy(totalSize, handle, 0, this::writeModels);
	}

	private void uploadDraws(StagingBuffer stagingBuffer) {
		var totalSize = indirectDraws.size() * IndirectBuffers.DRAW_COMMAND_STRIDE;
		var handle = buffers.draw.handle();

		stagingBuffer.enqueueCopy(totalSize, handle, 0, this::writeCommands);
	}

	private void writeModels(long writePtr) {
		for (var batch : indirectModels) {
			batch.write(writePtr);
			writePtr += IndirectBuffers.MODEL_STRIDE;
		}
	}

	private void writeCommands(long writePtr) {
		for (var batch : indirectDraws) {
			batch.write(writePtr);
			writePtr += IndirectBuffers.DRAW_COMMAND_STRIDE;
		}
	}

	public void delete() {
		buffers.delete();
		meshPool.delete();
	}

	private record MultiDraw(Material material, int start, int end) {
		void submit() {
			MaterialRenderState.setup(material);
			glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, start * IndirectBuffers.DRAW_COMMAND_STRIDE, end - start, (int) IndirectBuffers.DRAW_COMMAND_STRIDE);
		}
	}
}
