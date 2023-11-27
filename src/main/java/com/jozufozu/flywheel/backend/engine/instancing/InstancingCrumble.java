package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.compile.InstancingPrograms;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.mojang.blaze3d.systems.RenderSystem;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;

public class InstancingCrumble {
	@NotNull
	public static Map<ShaderState, Int2ObjectMap<Map<BlockPos, List<Runnable>>>> doCrumblingSort(List<Engine.CrumblingBlock> instances) {
		Map<ShaderState, Int2ObjectMap<Map<BlockPos, List<Runnable>>>> out = new HashMap<>();

		for (Engine.CrumblingBlock triple : instances) {
			int progress = triple.progress();

			if (progress < 0 || progress >= ModelBakery.DESTROY_TYPES.size()) {
				continue;
			}

			BlockPos position = triple.pos();

			for (Instance instance : triple.instances()) {
				// Filter out instances that weren't created by this engine.
				// If all is well, we probably shouldn't take the `continue`
				// branches but better to do checked casts.
				if (!(instance.handle() instanceof InstanceHandleImpl impl)) {
					continue;
				}
				if (!(impl.instancer instanceof InstancedInstancer<?> instancer)) {
					continue;
				}

				List<DrawCall> draws = instancer.drawCalls();

				draws.removeIf(DrawCall::isInvalid);

				for (DrawCall draw : draws) {
					out.computeIfAbsent(draw.shaderState, $ -> new Int2ObjectArrayMap<>())
							.computeIfAbsent(progress, $ -> new HashMap<>())
							.computeIfAbsent(position, $ -> new ArrayList<>())
							.add(() -> draw.renderOne(impl));
				}
			}
		}
		return out;
	}

	public static void render(List<Engine.CrumblingBlock> crumblingBlocks, BlockPos renderOrigin) {
		// Sort draw calls into buckets, so we don't have to do as many shader binds.
		var byShaderState = doCrumblingSort(crumblingBlocks);

		if (byShaderState.isEmpty()) {
			return;
		}

		try (var state = GlStateTracker.getRestoreState()) {
			for (var shaderStateEntry : byShaderState.entrySet()) {
				var byProgress = shaderStateEntry.getValue();

				if (byProgress.isEmpty()) {
					continue;
				}

				ShaderState shader = shaderStateEntry.getKey();

				var material = shader.material();

				var program = InstancingPrograms.get()
						.get(shader.vertexType(), shader.instanceType(), Contexts.CRUMBLING);
				UniformBuffer.syncAndBind(program);

				int crumblingBlockPosUniform = program.getUniformLocation("_flw_crumblingBlockPos");

				InstancingEngine.uploadMaterialIDUniform(program, material);

				int renderTex = getDiffuseTexture(material);

				for (Int2ObjectMap.Entry<Map<BlockPos, List<Runnable>>> progressEntry : byProgress.int2ObjectEntrySet()) {
					var byPos = progressEntry.getValue();

					if (byPos.isEmpty()) {
						continue;
					}

					var crumblingType = ModelBakery.DESTROY_TYPES.get(progressEntry.getIntKey());

					crumblingType.setupRenderState();

					RenderSystem.setShaderTexture(1, renderTex);
					GlTextureUnit.T1.makeActive();
					RenderSystem.bindTexture(renderTex);

					for (var blockPosEntry : byPos.entrySet()) {
						var center = blockPosEntry.getKey().getCenter();
						GL32.glUniform3f(crumblingBlockPosUniform, (float) center.x - renderOrigin.getX(), (float) center.y - renderOrigin.getY(), (float) center.z - renderOrigin.getZ());

						blockPosEntry.getValue().forEach(Runnable::run);
					}
				}
			}
		}
	}

	private static int getDiffuseTexture(Material material) {
		material.setup();

		int out = RenderSystem.getShaderTexture(0);

		material.clear();
		return out;
	}
}
