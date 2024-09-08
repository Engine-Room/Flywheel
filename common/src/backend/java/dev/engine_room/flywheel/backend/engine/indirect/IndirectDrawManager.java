package dev.engine_room.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL40.glDrawElementsIndirect;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BARRIER_BIT;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.ContextShader;
import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.engine.CommonCrumbling;
import dev.engine_room.flywheel.backend.engine.DrawManager;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.engine.TextureBinder;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import net.minecraft.client.resources.model.ModelBakery;

public class IndirectDrawManager extends DrawManager<IndirectInstancer<?>> {
	private final IndirectPrograms programs;
	private final StagingBuffer stagingBuffer;
	private final MeshPool meshPool;
	private final GlVertexArray vertexArray;
	private final Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();
	private final GlBuffer crumblingDrawBuffer = new GlBuffer();
	private final LightBuffers lightBuffers;
	private final MatrixBuffer matrixBuffer;

	private final DepthPyramid depthPyramid;
	private final VisibilityBuffer visibilityBuffer;

	private int totalPagesLastFrame = 0;

	private boolean needsBarrier = false;

	public IndirectDrawManager(IndirectPrograms programs) {
		this.programs = programs;
		programs.acquire();

		stagingBuffer = new StagingBuffer(this.programs);
		meshPool = new MeshPool();
		vertexArray = GlVertexArray.create();
		meshPool.bind(vertexArray);
		lightBuffers = new LightBuffers();
		matrixBuffer = new MatrixBuffer();

		depthPyramid = new DepthPyramid(programs.getDepthReduceProgram());
		visibilityBuffer = new VisibilityBuffer(programs.getReadVisibilityProgram());
	}

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstancerKey<I> key) {
		return new IndirectInstancer<>(key.type(), key.environment(), key.model());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, IndirectInstancer<?> instancer) {
		var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(key.type(), t -> new IndirectCullingGroup<>(t, programs));
		group.add((IndirectInstancer<I>) instancer, key, meshPool);
	}

	public boolean hasVisualType(VisualType visualType) {
		for (var group : cullingGroups.values()) {
			if (group.hasVisualType(visualType)) {
				return true;
			}
		}
		return false;
	}

	public void render(VisualType visualType) {
		// FIXME: Two pass occlusion prefers to render everything at once
		if (visualType != VisualType.BLOCK_ENTITY) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			TextureBinder.bindLightAndOverlay();

			vertexArray.bindForDraw();
			lightBuffers.bind();
			matrixBuffer.bind();
			Uniforms.bindAll();

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			visibilityBuffer.bind();

			for (var group1 : cullingGroups.values()) {
				group1.dispatchCull();
			}

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			dispatchApply();

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			visibilityBuffer.attach();

			submitDraws();

			depthPyramid.generate();

			programs.getZeroModelProgram()
					.bind();

			for (var group : cullingGroups.values()) {
				group.dispatchModelReset();
			}

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			GlTextureUnit.T0.makeActive();
			GlStateManager._bindTexture(depthPyramid.pyramidTextureId);

			for (var group1 : cullingGroups.values()) {
				group1.dispatchCullPassTwo();
			}

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			dispatchApply();

			glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

			submitDraws();

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();

			visibilityBuffer.detach();
		}
	}

	private void dispatchApply() {
		programs.getApplyProgram()
				.bind();

		for (var group1 : cullingGroups.values()) {
			group1.dispatchApply();
		}
	}

	private void submitDraws() {
		for (var group : cullingGroups.values()) {
			group.submit(VisualType.BLOCK_ENTITY);
			group.submit(VisualType.ENTITY);
			group.submit(VisualType.EFFECT);
		}
	}

	@Override
	public void flush(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
		super.flush(lightStorage, environmentStorage);

		for (var group : cullingGroups.values()) {
			group.flushInstancers();
		}

		visibilityBuffer.read(totalPagesLastFrame);
		visibilityBuffer.clear();

		cullingGroups.values()
				.removeIf(IndirectCullingGroup::checkEmptyAndDelete);

		instancers.values()
				.removeIf(instancer -> instancer.instanceCount() == 0);

		int totalPagesThisFrame = 0;
		for (var group : cullingGroups.values()) {
			totalPagesThisFrame += group.flipVisibilityOffsets(totalPagesThisFrame);
		}

		meshPool.flush();

		stagingBuffer.reclaim();

		lightBuffers.flush(stagingBuffer, lightStorage);

		matrixBuffer.flush(stagingBuffer, environmentStorage);

		for (var group : cullingGroups.values()) {
			group.upload(stagingBuffer);
		}

		stagingBuffer.flush();

		// We could probably save some driver calls here when there are
		// actually zero instances, but that feels like a very rare case

		needsBarrier = true;

		totalPagesLastFrame = totalPagesThisFrame;
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

		depthPyramid.delete();

		visibilityBuffer.delete();
	}

	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		var byType = doCrumblingSort(IndirectInstancer.class, crumblingBlocks);

		if (byType.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			TextureBinder.bindLightAndOverlay();

			vertexArray.bindForDraw();
			Uniforms.bindAll();

			var crumblingMaterial = SimpleMaterial.builder();

			// Scratch memory for writing draw commands.
			var block = MemoryBlock.malloc(IndirectBuffers.DRAW_COMMAND_STRIDE);

			GlBufferType.DRAW_INDIRECT_BUFFER.bind(crumblingDrawBuffer.handle());
			glBindBufferRange(GL_SHADER_STORAGE_BUFFER, BufferBindings.DRAW, crumblingDrawBuffer.handle(), 0, IndirectBuffers.DRAW_COMMAND_STRIDE);

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
