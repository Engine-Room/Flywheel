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
import com.jozufozu.flywheel.backend.MaterialUtil;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.model.ModelUtil;

public class IndirectCullingGroup<I extends Instance> {
	private static final int DRAW_BARRIER_BITS = GL_SHADER_STORAGE_BARRIER_BIT | GL_COMMAND_BARRIER_BIT;

	private final GlProgram cull;
	private final GlProgram draw;
	private final long objectStride;
	private final IndirectBuffers buffers;
	public final IndirectMeshPool meshPool;
	private final List<IndirectModel> indirectModels = new ArrayList<>();
	private final List<IndirectDraw> indirectDraws = new ArrayList<>();
	private final Map<RenderStage, List<MultiDraw>> multiDraws = new EnumMap<>(RenderStage.class);
	private boolean needsDrawBarrier;
	private boolean needsSortDraws;
	private int instanceCountThisFrame;
	private final GlProgram apply;

	IndirectCullingGroup(InstanceType<I> instanceType) {
		objectStride = instanceType.getLayout()
				.getStride() + IndirectBuffers.INT_SIZE;

		buffers = new IndirectBuffers(objectStride);
		buffers.createBuffers();

		meshPool = new IndirectMeshPool();

		var indirectPrograms = IndirectPrograms.get();
		cull = indirectPrograms.getCullingProgram(instanceType);
		apply = indirectPrograms.getApplyProgram();
		draw = indirectPrograms.getIndirectProgram(instanceType, Contexts.WORLD);
	}

	public void add(IndirectInstancer<I> instancer, RenderStage stage, Model model) {
		var meshes = model.getMeshes();

		var boundingSphere = ModelUtil.computeBoundingSphere(meshes.values());

		int modelID = indirectModels.size();
		var indirectModel = new IndirectModel(instancer, modelID, boundingSphere);
		indirectModels.add(indirectModel);

		for (Map.Entry<Material, Mesh> materialMeshEntry : meshes.entrySet()) {
			IndirectMeshPool.BufferedMesh bufferedMesh = meshPool.alloc(materialMeshEntry.getValue());
			indirectDraws.add(new IndirectDraw(indirectModel, materialMeshEntry.getKey(), bufferedMesh, stage));
		}

		needsSortDraws = true;
	}

	private void sortDraws() {
		multiDraws.clear();
		// sort by stage, then material
		indirectDraws.sort(Comparator.comparing(IndirectDraw::stage)
				.thenComparing(IndirectDraw::material, MaterialUtil.BY_STATE));

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

	public void flush() {
		needsDrawBarrier = true;
		instanceCountThisFrame = calculateTotalInstanceCountAndPrepareBatches();

		if (nothingToDo()) {
			return;
		}

		buffers.updateCounts(instanceCountThisFrame, indirectDraws.size(), indirectModels.size());

		if (needsSortDraws) {
			sortDraws();
			needsSortDraws = false;
		}

		meshPool.flush();
		uploadInstances();
		uploadModels();
		uploadIndirectCommands();
	}

	public void dispatchCull() {
		if (nothingToDo()) {
			return;
		}
		UniformBuffer.syncAndBind(cull);
		buffers.bindForCompute();
		glDispatchCompute(getGroupCount(instanceCountThisFrame), 1, 1);
	}

	public void dispatchApply() {
		if (nothingToDo()) {
			return;
		}
		apply.bind();
		buffers.bindForCompute();
		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
		glDispatchCompute(getGroupCount(indirectDraws.size()), 1, 1);
	}

	private boolean nothingToDo() {
		return indirectDraws.isEmpty() || instanceCountThisFrame == 0;
	}

	private boolean nothingToDo(RenderStage stage) {
		return nothingToDo() || !multiDraws.containsKey(stage);
	}

	public void submit(RenderStage stage) {
		if (nothingToDo(stage)) {
			return;
		}

		UniformBuffer.syncAndBind(draw);
		meshPool.bindForDraw();
		buffers.bindForDraw();

		drawBarrier();

		var flwBaseDraw = draw.getUniformLocation("_flw_baseDraw");

		for (var multiDraw : multiDraws.get(stage)) {
			glUniform1ui(flwBaseDraw, multiDraw.start);
			multiDraw.submit();
		}
		MaterialUtil.reset();
	}

	private void drawBarrier() {
		if (needsDrawBarrier) {
			glMemoryBarrier(DRAW_BARRIER_BITS);
			needsDrawBarrier = false;
		}
	}

	private void uploadInstances() {
		long objectPtr = buffers.objectPtr;

		for (IndirectModel batch : indirectModels) {
			var instanceCount = batch.instancer.getInstanceCount();
			batch.writeObjects(objectPtr);

			objectPtr += instanceCount * objectStride;
		}

		buffers.flushObjects(objectPtr - buffers.objectPtr);
	}

	private void uploadModels() {
		long writePtr = buffers.modelPtr.ptr();
		for (var batch : indirectModels) {
			batch.writeModel(writePtr);
			writePtr += IndirectBuffers.MODEL_STRIDE;
		}
		buffers.flushModels(writePtr - buffers.modelPtr.ptr());
	}

	private void uploadIndirectCommands() {
		long writePtr = buffers.drawPtr.ptr();
		for (var batch : indirectDraws) {
			batch.writeIndirectCommand(writePtr);
			writePtr += IndirectBuffers.DRAW_COMMAND_STRIDE;
		}
		buffers.flushDrawCommands(writePtr - buffers.drawPtr.ptr());
	}

	private int calculateTotalInstanceCountAndPrepareBatches() {
		int baseInstance = 0;
		for (var batch : indirectModels) {
			batch.prepare(baseInstance);
			baseInstance += batch.instancer.getInstanceCount();
		}
		return baseInstance;
	}

	public void delete() {
		buffers.delete();
		meshPool.delete();
	}

	public boolean hasStage(RenderStage stage) {
		return multiDraws.containsKey(stage);
	}

	private static int getGroupCount(int threadCount) {
		if (GlCompat.amd) {
			return (threadCount + 63) >> 6; // ceil(threadCount / 64)
		} else {
			return (threadCount + 31) >> 5; // ceil(threadCount / 32)
		}
	}

	private record MultiDraw(Material material, int start, int end) {
		void submit() {
			MaterialUtil.setup(material);
			glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, start * IndirectBuffers.DRAW_COMMAND_STRIDE, end - start, (int) IndirectBuffers.DRAW_COMMAND_STRIDE);
		}
	}
}
