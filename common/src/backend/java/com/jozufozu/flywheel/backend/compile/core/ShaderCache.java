package com.jozufozu.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.glsl.GlslVersion;
import com.jozufozu.flywheel.backend.glsl.SourceComponent;

public class ShaderCache {
	private final Map<ShaderKey, ShaderResult> inner = new HashMap<>();
	private final CompilerStats stats;

	public ShaderCache(CompilerStats stats) {
		this.stats = stats;
	}

	@Nullable
	public GlShader compile(GlslVersion glslVersion, ShaderType shaderType, String name, Consumer<Compilation> callback, List<SourceComponent> sourceComponents) {
		var key = new ShaderKey(glslVersion, shaderType, name);
		var cached = inner.get(key);
		if (cached != null) {
			return cached.unwrap();
		}

		Compilation ctx = new Compilation();
		ctx.version(glslVersion);
		ctx.define(shaderType.define);

		callback.accept(ctx);

		expand(sourceComponents, ctx::appendComponent);

		ShaderResult out = ctx.compile(shaderType, name);
		inner.put(key, out);
		stats.shaderResult(out);
		return out.unwrap();
	}

	public void delete() {
		inner.values()
				.stream()
				.map(ShaderResult::unwrap)
				.filter(Objects::nonNull)
				.forEach(GlShader::delete);
	}

	private static void expand(List<SourceComponent> rootSources, Consumer<SourceComponent> out) {
		var included = new LinkedHashSet<SourceComponent>(); // use hash set to deduplicate. linked to preserve order
		for (var component : rootSources) {
			recursiveDepthFirstInclude(included, component);
			included.add(component);
		}
		included.forEach(out);
	}

	private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
		for (var include : component.included()) {
			recursiveDepthFirstInclude(included, include);
		}
		included.addAll(component.included());
	}

	private record ShaderKey(GlslVersion glslVersion, ShaderType shaderType, String name) {
	}
}
