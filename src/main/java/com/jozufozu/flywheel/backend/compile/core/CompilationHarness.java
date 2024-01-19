package com.jozufozu.flywheel.backend.compile.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.backend.glsl.ShaderSources;

public class CompilationHarness<K> {
	private final KeyCompiler<K> compiler;
	private final SourceLoader sourceLoader;
	private final ShaderCompiler shaderCompiler;
	private final ProgramLinker programLinker;
	private final CompilerStats stats;

	public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
		this.compiler = compiler;
		stats = new CompilerStats(marker);
		sourceLoader = new SourceLoader(sources, stats);
		shaderCompiler = new ShaderCompiler(stats);
		programLinker = new ProgramLinker(stats);
	}

	@Nullable
	public Map<K, GlProgram> compileAndReportErrors(Collection<K> keys) {
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
			stats.emitErrorLog();
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
