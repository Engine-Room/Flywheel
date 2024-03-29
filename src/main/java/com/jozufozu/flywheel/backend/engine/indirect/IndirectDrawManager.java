package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL40.glDrawElementsIndirect;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.backend.Samplers;
import com.jozufozu.flywheel.backend.compile.ContextShader;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.CommonCrumbling;
import com.jozufozu.flywheel.backend.engine.DrawManager;
import com.jozufozu.flywheel.backend.engine.GroupKey;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.MaterialRenderState;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.engine.TextureBinder;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.lib.material.SimpleMaterial;
import com.jozufozu.flywheel.lib.memory.MemoryBlock;

import net.minecraft.client.resources.model.ModelBakery;

public class IndirectDrawManager extends DrawManager<IndirectInstancer<?>> {
	private final IndirectPrograms programs;
	private final StagingBuffer stagingBuffer;
	private final MeshPool meshPool;
	private final GlVertexArray vertexArray;
	private final Map<GroupKey<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();
	private final GlBuffer crumblingDrawBuffer = new GlBuffer();

	public IndirectDrawManager(IndirectPrograms programs) {
		this.programs = programs;
		programs.acquire();
		stagingBuffer = new StagingBuffer(this.programs);

		meshPool = new MeshPool();

		vertexArray = GlVertexArray.create();

		meshPool.bind(vertexArray);
	}

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstancerKey<I> key) {
		return new IndirectInstancer<>(key.type(), key.environment(), key.model());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, IndirectInstancer<?> instancer) {
		var groupKey = new GroupKey<>(key.type(), key.environment());
		var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(groupKey, t -> new IndirectCullingGroup<>(t.instanceType(), t.environment(), programs));
		group.add((IndirectInstancer<I>) instancer, key.model(), key.stage(), meshPool);
	}

	public boolean hasStage(RenderStage stage) {
		for (var group : cullingGroups.values()) {
			if (group.hasStage(stage)) {
				return true;
			}
		}
		return false;
	}

	public void renderStage(RenderStage stage) {
		if (!hasStage(stage)) {
			return;
		}

		try (var restoreState = GlStateTracker.getRestoreState()) {
			TextureBinder.bindLightAndOverlay();

			vertexArray.bindForDraw();
			Uniforms.bindForDraw();

			for (var group : cullingGroups.values()) {
				group.submit(stage);
			}

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();
		}
	}

	@Override
	public void flush() {
		super.flush();

		for (var group : cullingGroups.values()) {
			group.flushInstancers();
		}

		instancers.values().removeIf(instancer -> instancer.instanceCount() == 0);

		meshPool.flush();

		stagingBuffer.reclaim();

		for (var group : cullingGroups.values()) {
			group.upload(stagingBuffer);
		}

		stagingBuffer.flush();

		for (var group : cullingGroups.values()) {
			group.dispatchCull();
		}

		for (var group : cullingGroups.values()) {
			group.dispatchApply();
		}
	}

	@Override
	public void delete() {
		super.delete();

		cullingGroups.values()
				.forEach(IndirectCullingGroup::delete);
		cullingGroups.clear();

		stagingBuffer.delete();

		meshPool.delete();

		crumblingDrawBuffer.delete();

		programs.release();
	}

	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		var byType = doCrumblingSort(IndirectInstancer.class, crumblingBlocks);

		if (byType.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			TextureBinder.bindLightAndOverlay();

			vertexArray.bindForDraw();
			Uniforms.bindForDraw();

			var crumblingMaterial = SimpleMaterial.builder();

			// Scratch memory for writing draw commands.
			var block = MemoryBlock.malloc(IndirectBuffers.DRAW_COMMAND_STRIDE);

			GlBufferType.DRAW_INDIRECT_BUFFER.bind(crumblingDrawBuffer.handle());
			glBindBufferRange(GL_SHADER_STORAGE_BUFFER, IndirectBuffers.DRAW_INDEX, crumblingDrawBuffer.handle(), 0, IndirectBuffers.DRAW_COMMAND_STRIDE);

			for (var groupEntry : byType.entrySet()) {
				var byProgress = groupEntry.getValue();

				// Set up the crumbling program buffers. Nothing changes here between draws.
				cullingGroups.get(groupEntry.getKey())
						.bindWithContextShader(ContextShader.CRUMBLING);

				for (var progressEntry : byProgress.int2ObjectEntrySet()) {
					Samplers.CRUMBLING.makeActive();
					TextureBinder.bind(ModelBakery.BREAKING_LOCATIONS.get(progressEntry.getIntKey()));

					for (var instanceHandlePair : progressEntry.getValue()) {
						IndirectInstancer<?> instancer = instanceHandlePair.first();
						int instanceIndex = instanceHandlePair.second().index;

						for (IndirectDraw draw : instancer.draws()) {
							// Transform the material to be suited for crumbling.
							CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());

							MaterialRenderState.setup(crumblingMaterial);

							// Upload the draw command.
							draw.writeWithOverrides(block.ptr(), instanceIndex, crumblingMaterial);
							crumblingDrawBuffer.upload(block);

							// Submit! Everything is already bound by here.
							glDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0);
						}
					}

				}
			}

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();

			block.free();
		}
	}
}
