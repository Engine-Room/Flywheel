package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.gl.GlObject;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;

import net.minecraft.resources.ResourceLocation;

/**
 * Handles compilation and deletion of vertex shaders.
 */
public class ShaderCompiler {

	private final Map<ShaderContext, GlShader> map = new HashMap<>();

	ShaderCompiler() {
	}

	public GlShader get(ShaderContext key) {
		return map.computeIfAbsent(key, this::_create);
	}

	protected GlShader _create(ShaderContext key) {
		StringBuilder finalSource = new StringBuilder(key.generateHeader());
		finalSource.append("#extension GL_ARB_explicit_attrib_location : enable\n");
		finalSource.append("#extension GL_ARB_conservative_depth : enable\n");

		var ctx = new CompilationContext();

		var names = ImmutableList.<ResourceLocation>builder();
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : key.sourceComponents()) {
			included.addAll(component.included());
			names.add(component.name());
		}

		for (var include : included) {
			finalSource.append(include.source(ctx));
		}

		for (var component : key.sourceComponents()) {
			finalSource.append(component.source(ctx));
		}

		try {
			return new GlShader(finalSource.toString(), key.shaderType(), names.build());
		} catch (ShaderCompilationException e) {
			throw e.withErrorLog(ctx);
		}
	}

	public void invalidate() {
		map.values()
				.forEach(GlObject::delete);
		map.clear();
	}

}
