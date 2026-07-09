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

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.IndirectPrograms;
import dev.engine_room.flywheel.backend.compile.PipelineCompiler;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.CommonCrumbling;
import dev.engine_room.flywheel.backend.engine.DrawManager;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.InstancerKey;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import dev.engine_room.flywheel.backend.engine.TextureBinder;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferUsage;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;

public class IndirectDrawManager extends DrawManager<IndirectInstancer<?>> {
	private final IndirectPrograms programs;
	private final StagingBuffer stagingBuffer;
	private final MeshPool meshPool;
	private final GlVertexArray vertexArray;
	private final Map<InstanceType<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();
	private final GlBuffer crumblingDrawBuffer = new GlBuffer(GlBufferUsage.STREAM_DRAW);
	private final LightBuffers lightBuffers;
	private final MatrixBuffer matrixBuffer;

	private final DepthPyramid depthPyramid;

	private final OitFramebuffer oitFramebuffer;

	public IndirectDrawManager(IndirectPrograms programs) {
		this.programs = programs;
		programs.acquire();

		// WARN: We should avoid eagerly grabbing GlPrograms here as catching compile
		// errors and falling back during construction is a bit more complicated.
		stagingBuffer = new StagingBuffer(this.programs);
		meshPool = new MeshPool();
		vertexArray = GlVertexArray.create();
		meshPool.bind(vertexArray);
		lightBuffers = new LightBuffers();
		matrixBuffer = new MatrixBuffer();

		depthPyramid = new DepthPyramid(programs);

		oitFramebuffer = new OitFramebuffer(programs.oitPrograms());
	}

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstancerKey<I> key) {
		return new IndirectInstancer<>(key, new AbstractInstancer.Recreate<>(key, this));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, IndirectInstancer<?> instancer) {
		var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(key.type(), t -> new IndirectCullingGroup<>(t, programs));
		group.add((IndirectInstancer<I>) instancer, key, meshPool);
	}

	@Override
	public void render(LightStorage lightStorage, EnvironmentStorage environmentStorage) {
		super.render(lightStorage, environmentStorage);

		// Flush instance counts, page mappings, and prune empty groups.
		cullingGroups.values()
				.removeIf(IndirectCullingGroup::flushInstancers);

		// Instancers may have been emptied in the above call, now remove them here.
		instancers.values()
				.removeIf(instancer -> instancer.instanceCount() == 0);

		meshPool.flush();

		stagingBuffer.reclaim();

		// Genuinely nothing to do, we can just early out.
		// Still process the mesh pool and reclaim fenced staging regions though.
		if (cullingGroups.isEmpty()) {
			return;
		}

		lightBuffers.flush(stagingBuffer, lightStorage);

		matrixBuffer.flush(stagingBuffer, environmentStorage);

		for (var group : cullingGroups.values()) {
			group.upload(stagingBuffer);
		}

		stagingBuffer.flush();

		depthPyramid.generate();

		// We could probably save some driver calls here when there are
		// actually zero instances, but that feels like a very rare case

		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

		matrixBuffer.bind();

		depthPyramid.bindForCull();

		for (var group : cullingGroups.values()) {
			group.dispatchCull();
		}

		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

		programs.getApplyProgram()
				.bind();

		for (var group : cullingGroups.values()) {
			group.dispatchApply();
		}

		glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

		TextureBinder.bindLightAndOverlay();

		vertexArray.bindForDraw();
		lightBuffers.bind();
		matrixBuffer.bind();
		Uniforms.bindAll();

		for (var group : cullingGroups.values()) {
			group.submitSolid();
		}

		// Let's avoid invoking the oit chain if we don't have anything to do
		boolean useOit = false;
		for (var group : cullingGroups.values()) {
			if (group.hasOitDraws()) {
				useOit = true;
				break;
			}
		}

		if (useOit) {
			oitFramebuffer.prepare();

			oitFramebuffer.depthRange();

			for (var group : cullingGroups.values()) {
				group.submitTransparent(PipelineCompiler.OitMode.DEPTH_RANGE);
			}

			oitFramebuffer.renderTransmittance();

			for (var group : cullingGroups.values()) {
				group.submitTransparent(PipelineCompiler.OitMode.GENERATE_COEFFICIENTS);
			}

			oitFramebuffer.renderDepthFromTransmittance();

			// Need to bind this again because we just drew a full screen quad for OIT.
			vertexArray.bindForDraw();

			oitFramebuffer.accumulate();

			for (var group : cullingGroups.values()) {
				group.submitTransparent(PipelineCompiler.OitMode.EVALUATE);
			}

			oitFramebuffer.composite();
		}

		MaterialRenderState.reset();
		TextureBinder.resetLightAndOverlay();
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

		lightBuffers.delete();

		matrixBuffer.delete();

		oitFramebuffer.delete();
	}

	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		var byType = doCrumblingSort(crumblingBlocks, IndirectInstancer::fromState);

		if (byType.isEmpty()) {
			return;
		}

		TextureBinder.bindLightAndOverlay();

		vertexArray.bindForDraw();
		Uniforms.bindAll();

		var crumblingMaterial = SimpleMaterial.builder();

		// Scratch memory for writing draw commands.
		var block = MemoryBlock.malloc(IndirectBuffers.DRAW_COMMAND_STRIDE);

		// Set up the crumbling program buffers. Nothing changes here between draws.
		GlBufferType.DRAW_INDIRECT_BUFFER.bind(crumblingDrawBuffer.handle());
		glBindBufferRange(GL_SHADER_STORAGE_BUFFER, BufferBindings.DRAW, crumblingDrawBuffer.handle(), 0, IndirectBuffers.DRAW_COMMAND_STRIDE);

		for (var groupEntry : byType.entrySet()) {
			var byProgress = groupEntry.getValue();

			GroupKey<?> groupKey = groupEntry.getKey();
			IndirectCullingGroup<?> cullingGroup = cullingGroups.get(groupKey.instanceType());

			if (cullingGroup == null) {
				continue;
			}

			for (var progressEntry : byProgress.int2ObjectEntrySet()) {
				Samplers.CRUMBLING.makeActive();
				TextureBinder.bind(ModelBakery.BREAKING_LOCATIONS.get(progressEntry.getIntKey()));

				for (var instanceHandlePair : progressEntry.getValue()) {
					IndirectInstancer<?> instancer = instanceHandlePair.getFirst();
					int instanceIndex = instanceHandlePair.getSecond().index;

					for (IndirectDraw draw : instancer.draws()) {
						// Transform the material to be suited for crumbling.
						CommonCrumbling.applyCrumblingProperties(crumblingMaterial, draw.material());

						cullingGroup.bindForCrumbling(crumblingMaterial);

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

	@Override
	public void triggerFallback() {
		IndirectPrograms.kill();
		Minecraft.getInstance().levelRenderer.resetLevelRenderData();
	}

	@Override
	public MeshPool meshPool() {
		return meshPool;
	}
}
