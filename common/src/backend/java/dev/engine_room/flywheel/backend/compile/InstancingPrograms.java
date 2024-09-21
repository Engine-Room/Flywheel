package dev.engine_room.flywheel.backend.compile;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.material.MaterialShaders;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.backend.util.AtomicReferenceCounted;

public class InstancingPrograms extends AtomicReferenceCounted {
	private static final List<String> EXTENSIONS = getExtensions(GlCompat.MAX_GLSL_VERSION);

	@Nullable
	private static InstancingPrograms instance;

	private final CompilationHarness<PipelineProgramKey> pipeline;

	private InstancingPrograms(CompilationHarness<PipelineProgramKey> pipeline) {
		this.pipeline = pipeline;
	}

	private static List<String> getExtensions(GlslVersion glslVersion) {
		var extensions = ImmutableList.<String>builder();
		if (glslVersion.compareTo(GlslVersion.V330) < 0) {
			extensions.add("GL_ARB_shader_bit_encoding");
		}
		return extensions.build();
	}

	static void reload(ShaderSources sources, List<SourceComponent> vertexComponents, List<SourceComponent> fragmentComponents) {
		if (!GlCompat.SUPPORTS_INSTANCING) {
			return;
		}


		var pipelineCompiler = PipelineCompiler.create(sources, Pipelines.INSTANCING, vertexComponents, fragmentComponents, EXTENSIONS);
		InstancingPrograms newInstance = new InstancingPrograms(pipelineCompiler);

		setInstance(newInstance);
	}

	static void setInstance(@Nullable InstancingPrograms newInstance) {
		if (instance != null) {
			instance.release();
		}
		if (newInstance != null) {
			newInstance.acquire();
		}
		instance = newInstance;
	}

	@Nullable
	public static InstancingPrograms get() {
		return instance;
	}

	public static boolean allLoaded() {
		return instance != null;
	}

	public static void kill() {
		setInstance(null);
	}

	public GlProgram get(InstanceType<?> instanceType, ContextShader contextShader, LightShader light, CutoutShader cutout, MaterialShaders materialShaders) {
		return pipeline.get(new PipelineProgramKey(instanceType, contextShader, light, cutout, materialShaders));
	}

	@Override
	protected void _delete() {
		pipeline.delete();
	}
}
