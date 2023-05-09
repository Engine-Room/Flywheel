package com.jozufozu.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.gl.shader.GlShader;
import com.jozufozu.flywheel.gl.shader.ShaderType;
import com.jozufozu.flywheel.glsl.GLSLVersion;
import com.jozufozu.flywheel.glsl.SourceComponent;

public class ShaderCompiler {
	private final Map<ShaderKey, ShaderResult> shaderCache = new HashMap<>();
	private final CompilerStats stats;

	public ShaderCompiler(CompilerStats stats) {
		this.stats = stats;
	}

	public int shaderCount() {
		return shaderCache.size();
	}

	@Nullable
	public GlShader compile(GLSLVersion glslVersion, ShaderType shaderType, List<SourceComponent> sourceComponents) {
		var key = new ShaderKey(glslVersion, shaderType, sourceComponents);
		var cached = shaderCache.get(key);
		if (cached != null) {
			return cached.unwrap();
		}

		ShaderResult out = compileUncached(new Compilation(glslVersion, shaderType), sourceComponents);
		shaderCache.put(key, out);
		stats.shaderResult(out);
		return out.unwrap();
	}

	public void delete() {
		shaderCache.values()
				.stream()
				.map(ShaderResult::unwrap)
				.filter(Objects::nonNull)
				.forEach(GlShader::delete);
	}

	@NotNull
	private ShaderResult compileUncached(Compilation ctx, List<SourceComponent> sourceComponents) {
		ctx.enableExtension("GL_ARB_explicit_attrib_location");
		ctx.enableExtension("GL_ARB_conservative_depth");

		expand(sourceComponents, ctx::appendComponent);

		return ctx.compile();
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

	private record ShaderKey(GLSLVersion glslVersion, ShaderType shaderType, List<SourceComponent> sourceComponents) {
	}
}
