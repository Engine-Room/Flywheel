package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.pipeline.PipelineShader;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.BackendTypes;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.source.ShaderLoadingException;
import com.jozufozu.flywheel.util.StringUtil;

class CompilationEnvironment {
	final VertexMaterialComponent vertexMaterialComponent;
	final FragmentMaterialComponent fragmentMaterialComponent;

	boolean needsCrash = false;

	final long compileStart = System.nanoTime();
	final Multimap<Set<UniformProvider>, PipelineContext> uniformProviderGroups = ArrayListMultimap.create();
	final List<PipelineContext> pipelineContexts = new ArrayList<>();

	CompilationEnvironment() {
		for (PipelineShader pipelineShader : BackendTypes.availablePipelineShaders()) {
			for (StructType<?> structType : ComponentRegistry.structTypes) {
				for (VertexType vertexType : ComponentRegistry.vertexTypes) {
					for (ContextShader contextShader : ComponentRegistry.contextShaders) {
						acknowledgeContext(new PipelineContext(vertexType, structType, contextShader, pipelineShader));
					}
				}
			}
		}
		this.vertexMaterialComponent = new VertexMaterialComponent(ComponentRegistry.materials.vertexSources());
		this.fragmentMaterialComponent = new FragmentMaterialComponent(ComponentRegistry.materials.fragmentSources());
	}

	private void acknowledgeContext(PipelineContext ctx) {
		uniformProviderGroups.put(ctx.uniformProviders(), ctx);

		pipelineContexts.add(ctx);
	}

	public void finish() {
		long compileEnd = System.nanoTime();

		Backend.LOGGER.info("Compiled " + pipelineContexts.size() + " programs in " + StringUtil.formatTime(compileEnd - compileStart));

		if (needsCrash) {
			throw new ShaderLoadingException("Compilation failed");
		}
	}
}
