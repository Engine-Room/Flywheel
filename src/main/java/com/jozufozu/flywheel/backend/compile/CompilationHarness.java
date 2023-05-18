package com.jozufozu.flywheel.backend.compile;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.compile.core.CompilerStats;
import com.jozufozu.flywheel.backend.compile.core.ProgramLinker;
import com.jozufozu.flywheel.backend.compile.core.ShaderCompiler;
import com.jozufozu.flywheel.gl.shader.GlProgram;
import com.jozufozu.flywheel.glsl.ShaderSources;

public class CompilationHarness<K> {
	private final KeyCompiler<K> compiler;
	private final SourceLoader sourceLoader;
	private final ShaderCompiler shaderCompiler;
	private final ProgramLinker programLinker;
	private final ImmutableList<K> keys;
	private final CompilerStats stats = new CompilerStats();

	public CompilationHarness(ShaderSources sources, ImmutableList<K> keys, KeyCompiler<K> compiler) {
		this.keys = keys;

		this.compiler = compiler;
		sourceLoader = new SourceLoader(sources, stats);
		shaderCompiler = new ShaderCompiler(stats);
		programLinker = new ProgramLinker(stats);
	}

	@Nullable
	public Map<K, GlProgram> compileAndReportErrors() {
		stats.start();
		Map<K, GlProgram> out = new HashMap<>();
		for (var key : keys) {
			GlProgram glProgram = compiler.compile(key, sourceLoader, shaderCompiler, programLinker);
			if (out != null && glProgram != null) {
				out.put(key, glProgram);
			} else {
				out = null; // Return null when a preloading error occurs.
			}
		}
		stats.finish();

		if (stats.errored()) {
			Flywheel.LOGGER.error(stats.generateErrorLog());
			return null;
		}

		return out;
	}

	public void delete() {
		shaderCompiler.delete();
	}

	public interface KeyCompiler<K> {
		@Nullable GlProgram compile(K key, SourceLoader loader, ShaderCompiler shaderCompiler, ProgramLinker programLinker);
	}
}
