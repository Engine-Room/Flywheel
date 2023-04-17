package com.jozufozu.flywheel.backend.compile;

import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glLinkProgram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.backend.compile.FlwPrograms.PipelineProgramKey;
import com.jozufozu.flywheel.backend.compile.pipeline.Pipeline;
import com.jozufozu.flywheel.backend.engine.indirect.IndirectComponent;
import com.jozufozu.flywheel.gl.GLSLVersion;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.ShaderLoadingException;
import com.jozufozu.flywheel.glsl.ShaderSources;
import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.glsl.generate.FnSignature;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.resources.ResourceLocation;

public class FlwCompiler {
	private final long compileStart = System.nanoTime();

	private final ShaderSources sources;

	private final ImmutableList<PipelineProgramKey> pipelineKeys;
	private final ImmutableList<InstanceType<?>> cullingKeys;

	private final ShaderCompiler shaderCompiler;
	private final List<FailedCompilation> errors = new ArrayList<>();

	private final MaterialAdapterComponent vertexMaterialComponent;
	private final MaterialAdapterComponent fragmentMaterialComponent;
	private final UniformComponent uniformComponent;

	private final Map<PipelineProgramKey, GlProgram> pipelinePrograms = new HashMap<>();
	private final Map<InstanceType<?>, GlProgram> cullingPrograms = new HashMap<>();

	public FlwCompiler(ShaderSources sources, ImmutableList<PipelineProgramKey> pipelineKeys, ImmutableList<InstanceType<?>> cullingKeys) {
		this.sources = sources;

		this.pipelineKeys = pipelineKeys;
		this.cullingKeys = cullingKeys;

		shaderCompiler = ShaderCompiler.builder()
				.errorConsumer(errors::add)
				.build();

		vertexMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("vertex_material_adapter"))
				.materialSources(MaterialIndices.getAllVertexShaders())
				.adapt(FnSignature.ofVoid("flw_materialVertex"))
				.switchOn(GlslExpr.variable("_flw_materialVertexID"))
				.build(sources);
		fragmentMaterialComponent = MaterialAdapterComponent.builder(Flywheel.rl("fragment_material_adapter"))
				.materialSources(MaterialIndices.getAllFragmentShaders())
				.adapt(FnSignature.ofVoid("flw_materialFragment"))
				.adapt(FnSignature.create()
						.returnType("bool")
						.name("flw_discardPredicate")
						.arg("vec4", "color")
						.build(), GlslExpr.literal(false))
				.adapt(FnSignature.create()
						.returnType("vec4")
						.name("flw_fogFilter")
						.arg("vec4", "color")
						.build(), GlslExpr.variable("color"))
				.switchOn(GlslExpr.variable("_flw_materialFragmentID"))
				.build(sources);
		uniformComponent = UniformComponent.builder(Flywheel.rl("uniforms"))
			.sources(ShaderUniforms.REGISTRY.getAll()
					.stream()
					.map(ShaderUniforms::uniformShader)
					.toList())
			.build(sources);
	}

	public FlwPrograms compile() {
		doCompilation();

		finish();

		return new FlwPrograms(pipelinePrograms, cullingPrograms);
	}

	private void doCompilation() {
		for (var key : pipelineKeys) {
			GlProgram glProgram = compilePipelineProgram(key);
			if (glProgram != null) {
				pipelinePrograms.put(key, glProgram);
			}
		}

		for (var key : cullingKeys) {
			GlProgram glProgram = compileCullingProgram(key);
			if (glProgram != null) {
				cullingPrograms.put(key, glProgram);
			}
		}
	}

	private static GlProgram link(int... shaders) {
		var handle = glCreateProgram();
		for (var shader : shaders) {
			glAttachShader(handle, shader);
		}
		glLinkProgram(handle);
		CompileUtil.checkLinkLog(handle);
		return new GlProgram(handle);
	}

	@Nullable
	private GlProgram compilePipelineProgram(PipelineProgramKey key) {
		var glslVersion = key.pipelineShader()
				.glslVersion();

		var vertex = shaderCompiler.compile(glslVersion, ShaderType.VERTEX, getVertexComponents(key));
		var fragment = shaderCompiler.compile(glslVersion, ShaderType.FRAGMENT, getFragmentComponents(key));

		if (vertex == null || fragment == null) {
			return null;
		}

		var glProgram = link(vertex.handle(), fragment.handle());
		key.contextShader()
			.onProgramLink(glProgram);
		return glProgram;
	}

	private ImmutableList<SourceComponent> getVertexComponents(PipelineProgramKey key) {
		var instanceAssembly = key.pipelineShader()
				.assembler()
				.assemble(new Pipeline.InstanceAssemblerContext(sources, key.vertexType(), key.instanceType()));

		var layout = sources.find(key.vertexType()
				.layoutShader());
		var instance = sources.find(key.instanceType()
				.instanceShader());
		var context = sources.find(key.contextShader()
				.vertexShader());
		var pipeline = sources.find(key.pipelineShader()
				.vertexShader());

		return ImmutableList.of(uniformComponent, vertexMaterialComponent, instanceAssembly, layout, instance, context, pipeline);
	}

	private ImmutableList<SourceComponent> getFragmentComponents(PipelineProgramKey key) {
		var context = sources.find(key.contextShader()
				.fragmentShader());
		var pipeline = sources.find(key.pipelineShader()
				.fragmentShader());
		return ImmutableList.of(uniformComponent, fragmentMaterialComponent, context, pipeline);
	}

	@Nullable
	private GlProgram compileCullingProgram(InstanceType<?> key) {
		var computeComponents = getComputeComponents(key);
		var result = shaderCompiler.compile(GLSLVersion.V460, ShaderType.COMPUTE, computeComponents);

		if (result == null) {
			return null;
		}

		return link(result.handle());
	}

	private ImmutableList<SourceComponent> getComputeComponents(InstanceType<?> instanceType) {
		var instanceAssembly = new IndirectComponent(sources, instanceType);
		var instance = sources.find(instanceType.instanceShader());
		var pipeline = sources.find(Files.INDIRECT_CULL);

		return ImmutableList.of(uniformComponent, instanceAssembly, instance, pipeline);
	}

	private void finish() {
		long compileEnd = System.nanoTime();
		int programCount = pipelineKeys.size() + cullingKeys.size();
		int shaderCount = shaderCompiler.shaderCount();
		int errorCount = errors.size();
		var elapsed = StringUtil.formatTime(compileEnd - compileStart);

		Flywheel.LOGGER.info("Compiled " + programCount + " programs and " + shaderCount + " shaders in " + elapsed + " with " + errorCount + " errors.");

		if (errorCount > 0) {
			var details = errors.stream()
					.map(FailedCompilation::getMessage)
					.collect(Collectors.joining("\n"));
			// TODO: disable backend instead of crashing if compilation fails
			throw new ShaderLoadingException("Compilation failed.\n" + details);
		}
	}

	public void delete() {
		shaderCompiler.delete();
	}

	private static final class Files {
		public static final ResourceLocation INDIRECT_CULL = Flywheel.rl("internal/indirect_cull.glsl");
	}
}
