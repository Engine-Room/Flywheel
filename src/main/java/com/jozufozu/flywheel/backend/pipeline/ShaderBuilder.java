package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class ShaderBuilder {

	public final ResourceLocation name;
	public final Template template;

	private SourceFile mainFile;
	private GLSLVersion version;

	private StringBuilder source;
	private StringBuilder defines;
	private CharSequence footer;

	public ShaderBuilder(ResourceLocation name, Template template) {
		this.name = name;
		this.template = template;
	}

	public ShaderBuilder setVersion(GLSLVersion version) {
		this.version = version;
		return this;
	}

	public ShaderBuilder setDefines(List<String> defs) {
		defines = new StringBuilder();

		for (String def : defs) {
			defines.append("#define ")
					.append(def)
					.append('\n');
		}
		return this;
	}

	public ShaderBuilder setFooter(CharSequence footer) {
		this.footer = footer;
		return this;
	}

	public ShaderBuilder setMainSource(SourceFile file) {
		if (mainFile == file) return this;

		mainFile = file;
		source = new StringBuilder();

		for (SourceFile includeFile : Includer.recurseIncludes(file)) {
			source.append(includeFile.getElidedSource());
		}
		source.append(file.getElidedSource());

		return this;
	}

	public GlShader compile(ResourceLocation name, ShaderType type) {

		StringBuilder finalSource = new StringBuilder();

		finalSource.append("#version ")
				.append(version)
				.append('\n')
				.append("#define ")
				.append(type.define)
				.append('\n')
				.append(defines != null ? defines : "")
				.append(source)
				.append(template.footer(type, mainFile));

		return new GlShader(name, type, finalSource);
	}
}
