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
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.SourceFile;

import net.minecraft.resources.ResourceLocation;

public class CullingCompiler extends AbstractCompiler<InstanceType<?>> {
	private final UniformComponent uniformComponent;
	private final SourceFile pipelineCompute;

	public CullingCompiler(ShaderSources sources, ImmutableList<InstanceType<?>> keys, UniformComponent uniformComponent) {
		super(sources, keys);

		this.uniformComponent = uniformComponent;
		pipelineCompute = sources.find(Files.INDIRECT_CULL);
	}

	@Nullable
	@Override
	protected GlProgram compile(InstanceType<?> key) {
		var computeComponents = getComputeComponents(key);
		var compute = shaderCompiler.compile(GLSLVersion.V460, ShaderType.COMPUTE, computeComponents);

		if (compute == null) {
			return null;
		}

		return programLinker.link(compute);
	}

	private ImmutableList<SourceComponent> getComputeComponents(InstanceType<?> instanceType) {
		var instanceAssembly = new IndirectComponent(sources, instanceType);
		var instance = sources.find(instanceType.instanceShader());

		return ImmutableList.of(uniformComponent, instanceAssembly, instance, pipelineCompute);
	}

	private static final class Files {
		public static final ResourceLocation INDIRECT_CULL = Flywheel.rl("internal/indirect_cull.glsl");
	}
}
