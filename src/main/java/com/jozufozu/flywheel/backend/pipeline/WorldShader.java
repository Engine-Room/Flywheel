package com.jozufozu.flywheel.backend.pipeline;

import java.util.List;
import java.util.Optional;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;

import net.minecraft.resources.ResourceLocation;

public class WorldShader {

	public final ResourceLocation name;
	public final Template<?> template;
	public final CharSequence header;

	public SourceFile mainFile;

	private CharSequence source;
	private StringBuilder defines;

	public WorldShader(ResourceLocation name, Template<?> template, FileResolution header) {
		this.name = name;
		this.template = template;
		this.header = Optional.ofNullable(header.getFile())
				.map(SourceFile::generateFinalSource)
				.orElse("");
	}

	public WorldShader setDefines(List<String> defs) {
		defines = new StringBuilder();

		for (String def : defs) {
			defines.append("#define ")
					.append(def)
					.append('\n');
		}
		return this;
	}

	public WorldShader setMainSource(SourceFile file) {
		if (mainFile == file) return this;

		mainFile = file;
		source = file.generateFinalSource();

		return this;
	}

	public GlShader compile(ShaderType type) {

		StringBuilder finalSource = new StringBuilder();

		finalSource.append("#version ")
				.append(template.getVersion())
				.append('\n')
				.append("#define ")
				.append(type.define) // special case shader type declaration
				.append('\n')
				.append(defines != null ? defines : "")
				.append(header)
				.append('\n')
				.append(source)
				.append('\n');

		template.generateTemplateSource(finalSource, type, mainFile);

		return new GlShader(name, type, finalSource);
	}

	public ProtoProgram createProgram() {
		return new ProtoProgram(this);
	}
}
