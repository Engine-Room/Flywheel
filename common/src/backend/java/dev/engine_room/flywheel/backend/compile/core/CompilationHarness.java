package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;

public class CompilationHarness<K> {
	private final ShaderSources sources;
	private final KeyCompiler<K> compiler;
	private final ShaderCache shaderCache;
	private final ProgramLinker programLinker;
	private final CompilerStats stats;

	private final Map<K, GlProgram> programs = new HashMap<>();

	public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
		this.sources = sources;
		this.compiler = compiler;
		stats = new CompilerStats(marker);
		shaderCache = new ShaderCache(stats);
		programLinker = new ProgramLinker(stats);
	}

	public GlProgram get(K key) {
		return programs.computeIfAbsent(key, this::compile);
	}

	private GlProgram compile(K key) {
		var out = compiler.compile(key, sources, shaderCache, programLinker);

		if (out == null) {
			// TODO: populate exception with error details
			throw new ShaderException();
		}

		return out;
	}

	public void delete() {
		shaderCache.delete();

		programs.values()
				.forEach(GlObject::delete);

		programs.clear();
	}

	public interface KeyCompiler<K> {
		@Nullable GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker);
	}
}
