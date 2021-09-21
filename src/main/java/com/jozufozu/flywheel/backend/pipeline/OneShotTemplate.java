package com.jozufozu.flywheel.backend.pipeline;

import java.util.Collection;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.SourceFile;

public class OneShotTemplate extends Template<OneShotProgramMetaData> {

	public static final OneShotTemplate INSTANCE = new OneShotTemplate();

	public OneShotTemplate() {
		super(OneShotProgramMetaData::new);
	}

	@Override
	public void generateTemplateSource(StringBuilder builder, ShaderType type, SourceFile file) {
		if (type == ShaderType.VERTEX) {
			vertexFooter(builder, file);
		} else if (type == ShaderType.FRAGMENT) {
			fragmentFooter(builder, file);
		}
	}

	@Override
	public Collection<ShaderInput> getShaderInputs(SourceFile file) {
		OneShotProgramMetaData data = getMetadata(file);

		return ShaderInput.fromStruct(data.vertex, "a_v_");
	}

	public void vertexFooter(StringBuilder template, SourceFile file) {
		OneShotProgramMetaData data = getMetadata(file);

		Template.prefixFields(template, data.vertex, "in", "a_v_");
		Template.prefixFields(template, data.interpolant, "out", "v2f_");

		template.append("void main() {\n");
		template.append(data.vertexName)
				.append(" v;\n");
		Template.assignFields(template, data.vertex, "v.", "a_v_");

		template.append(data.interpolantName)
				.append(" o = ")
				.append(data.vertexMain.call("v"))
				.append(";\n");

		Template.assignFields(template, data.interpolant, "v2f_", "o.");

		template.append('}');
	}

	public void fragmentFooter(StringBuilder template, SourceFile file) {
		OneShotProgramMetaData data = getMetadata(file);

		Template.prefixFields(template, data.interpolant, "in", "v2f_");

		template.append("void main() {\n");
		template.append(data.interpolant.name)
				.append(" o;\n");
		Template.assignFields(template, data.interpolant, "o.", "v2f_");

		template.append(data.fragmentMain.call("o"))
				.append(";\n");

		template.append('}');
	}
}
