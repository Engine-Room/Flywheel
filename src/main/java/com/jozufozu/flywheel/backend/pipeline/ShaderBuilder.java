package com.jozufozu.flywheel.backend.pipeline;

import java.util.List;

import com.jozufozu.flywheel.backend.FileResolution;
import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;

import net.minecraft.util.ResourceLocation;

public class ShaderBuilder {

	public final ResourceLocation name;
	public final ITemplate template;
	public final FileResolution header;

	public SourceFile mainFile;
	private GLSLVersion version;

	private StringBuilder source;
	private StringBuilder defines;

	public ShaderBuilder(ResourceLocation name, ITemplate template, FileResolution header) {
		this.name = name;
		this.template = template;
		this.header = header;
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

	public ShaderBuilder setMainSource(SourceFile file) {
		if (mainFile == file) return this;

		mainFile = file;
		source = new StringBuilder();

		file.generateFinalSource(source);

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
				.append(defines != null ? defines : "");
		SourceFile file = header.getFile();
		if (file != null) {
			file.generateFinalSource(finalSource);
		}
		mainFile.generateFinalSource(finalSource);
		template.generateTemplateSource(finalSource, type, mainFile);

		return new GlShader(name, type, finalSource);
	}
}
