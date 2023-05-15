package com.jozufozu.flywheel.backend.compile;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.backend.compile.component.IndirectComponent;
import com.jozufozu.flywheel.backend.compile.component.UniformComponent;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceFile;

import net.minecraft.resources.ResourceLocation;

public class CullingCompiler extends AbstractCompiler<InstanceType<?>> {
	private final UniformComponent uniformComponent;
	private final SourceFile pipelineCompute;

	public static CullingCompiler create(SourceLoader sourceLoader, ImmutableList<InstanceType<?>> keys, UniformComponent uniformComponent) {
		var sourceFile = sourceLoader.find(Files.INDIRECT_CULL);

		return new CullingCompiler(sourceLoader.sources, keys, uniformComponent, sourceFile);
	}

	private CullingCompiler(ShaderSources sources, ImmutableList<InstanceType<?>> keys, UniformComponent uniformComponent, SourceFile pipeline) {
		super(sources, keys);

		this.uniformComponent = uniformComponent;
		this.pipelineCompute = pipeline;
	}

	@Nullable
	@Override
	protected GlProgram compile(InstanceType<?> key) {
		var instanceAssembly = IndirectComponent.create(key);
		ResourceLocation rl = key.instanceShader();
		var instance = sourceLoader.find(rl);

		if (instance == null || uniformComponent == null || pipelineCompute == null) {
			return null;
		}

		var computeComponents = ImmutableList.of(uniformComponent, instanceAssembly, instance, pipelineCompute);
		var compute = shaderCompiler.compile(GLSLVersion.V460, ShaderType.COMPUTE, computeComponents);

		if (compute == null) {
			return null;
		}

		return programLinker.link(compute);
	}

	private static final class Files {
		public static final ResourceLocation INDIRECT_CULL = Flywheel.rl("internal/indirect_cull.glsl");
	}
}
