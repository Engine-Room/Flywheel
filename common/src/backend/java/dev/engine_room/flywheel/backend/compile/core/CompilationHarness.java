package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;

public class CompilationHarness<K> {
	private final KeyCompiler<K> compiler;
	private final SourceLoader sourceLoader;
	private final ShaderCache shaderCache;
	private final ProgramLinker programLinker;
	private final CompilerStats stats;

	private final Map<K, GlProgram> programs = new HashMap<>();

	public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
		this.compiler = compiler;
		stats = new CompilerStats(marker);
		sourceLoader = new SourceLoader(sources, stats);
		shaderCache = new ShaderCache(stats);
		programLinker = new ProgramLinker(stats);
	}

	public GlProgram get(K key) {
		return programs.computeIfAbsent(key, this::compile);
	}

	private GlProgram compile(K key) {
		var out = compiler.compile(key, sourceLoader, shaderCache, programLinker);

		if (out == null) {
			// TODO: populate exception with error details
			throw new ShaderException();
		}

		return out;
	}

	public void delete() {
		shaderCache.delete();

		for (var program : programs.values()) {
			program.delete();
		}
	}

	public interface KeyCompiler<K> {
		@Nullable GlProgram compile(K key, SourceLoader loader, ShaderCache shaderCache, ProgramLinker programLinker);
	}
}
