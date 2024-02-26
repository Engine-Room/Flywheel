package com.jozufozu.flywheel.backend.engine.indirect;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL40.glDrawElementsIndirect;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.backend.compile.ContextShaders;
import com.jozufozu.flywheel.backend.compile.IndirectPrograms;
import com.jozufozu.flywheel.backend.engine.CommonCrumbling;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.engine.InstancerKey;
import com.jozufozu.flywheel.backend.engine.InstancerStorage;
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
import com.jozufozu.flywheel.lib.util.Pair;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.ModelBakery;

public class IndirectDrawManager extends InstancerStorage<IndirectInstancer<?>> {
	private final IndirectPrograms programs;
	private final StagingBuffer stagingBuffer;
	private final MeshPool meshPool;
	private final GlVertexArray vertexArray;
	private final Map<GroupKey<?>, IndirectCullingGroup<?>> cullingGroups = new HashMap<>();
	private final GlBuffer crumblingDrawBuffer = new GlBuffer();

	public IndirectDrawManager(IndirectPrograms programs) {
		this.programs = programs;
		stagingBuffer = new StagingBuffer(this.programs);

		meshPool = new MeshPool();

		vertexArray = GlVertexArray.create();

		meshPool.bind(vertexArray);
	}

	@Override
	protected <I extends Instance> IndirectInstancer<?> create(InstancerKey<I> key) {
		return new IndirectInstancer<>(key.type(), key.embedding(), key.model());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <I extends Instance> void initialize(InstancerKey<I> key, IndirectInstancer<?> instancer) {
		var groupKey = new GroupKey<>(key.type(), key.embedding());
		var group = (IndirectCullingGroup<I>) cullingGroups.computeIfAbsent(groupKey, t -> new IndirectCullingGroup<>(t.type, t.context, programs));
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
		TextureBinder.bindLightAndOverlay();

		vertexArray.bindForDraw();
		Uniforms.bindForDraw();

		for (var group : cullingGroups.values()) {
			group.submit(stage);
		}

		MaterialRenderState.reset();
		TextureBinder.resetLightAndOverlay();
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
	}

	public void renderCrumbling(List<Engine.CrumblingBlock> crumblingBlocks) {
		var byType = doCrumblingSort(crumblingBlocks);

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

			for (var instanceTypeEntry : byType.entrySet()) {
				var byProgress = instanceTypeEntry.getValue();

				// Set up the crumbling program buffers. Nothing changes here between draws.
				var program = cullingGroups.get(instanceTypeEntry.getKey())
						.bindWithContextShader(ContextShaders.CRUMBLING);

				for (var progressEntry : byProgress.int2ObjectEntrySet()) {
					program.setTexture("crumblingTex", TextureBinder.byName(ModelBakery.BREAKING_LOCATIONS.get(progressEntry.getIntKey())));

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

					TextureBinder.resetTextureBindings();
				}
			}

			MaterialRenderState.reset();
			TextureBinder.resetLightAndOverlay();

			block.free();
		}
	}

	private static Map<GroupKey<?>, Int2ObjectMap<List<Pair<IndirectInstancer<?>, InstanceHandleImpl>>>> doCrumblingSort(List<Engine.CrumblingBlock> crumblingBlocks) {
		Map<GroupKey<?>, Int2ObjectMap<List<Pair<IndirectInstancer<?>, InstanceHandleImpl>>>> byType = new HashMap<>();
		for (Engine.CrumblingBlock block : crumblingBlocks) {
			int progress = block.progress();

			if (progress < 0 || progress >= ModelBakery.DESTROY_TYPES.size()) {
				continue;
			}

			for (Instance instance : block.instances()) {
				// Filter out instances that weren't created by this engine.
				// If all is well, we probably shouldn't take the `continue`
				// branches but better to do checked casts.
				if (!(instance.handle() instanceof InstanceHandleImpl impl)) {
					continue;
				}
				if (!(impl.instancer instanceof IndirectInstancer<?> instancer)) {
					continue;
				}

				byType.computeIfAbsent(new GroupKey<>(instancer.type, instancer.embedding), $ -> new Int2ObjectArrayMap<>())
						.computeIfAbsent(progress, $ -> new ArrayList<>())
						.add(Pair.of(instancer, impl));
			}
		}
		return byType;
	}

	public record GroupKey<I extends Instance>(InstanceType<I> type, @Nullable VisualEmbedding context) {
	}
}
