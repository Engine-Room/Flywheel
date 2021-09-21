package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.SourceFile;

public class InstancingTemplate extends Template<InstancingProgramMetaData> {

	public static final InstancingTemplate INSTANCE = new InstancingTemplate();

	public InstancingTemplate() {
		super(InstancingProgramMetaData::new);
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
		InstancingProgramMetaData data = getMetadata(file);

		List<ShaderInput> inputs = new ArrayList<>(ShaderInput.fromStruct(data.vertex, "a_v_"));
		inputs.addAll(ShaderInput.fromStruct(data.instance, "a_i_"));

		return inputs;
	}

	public void vertexFooter(StringBuilder template, SourceFile file) {
		InstancingProgramMetaData data = getMetadata(file);

		Template.prefixFields(template, data.vertex, "in", "a_v_");
		Template.prefixFields(template, data.instance, "in", "a_i_");
		Template.prefixFields(template, data.interpolant, "out", "v2f_");

		template.append("void main() {\n");
		template.append(data.vertexName)
				.append(" v;\n");
		Template.assignFields(template, data.vertex, "v.", "a_v_");

		template.append(data.instanceName)
				.append(" i;\n");
		Template.assignFields(template, data.instance, "i.", "a_i_");

		template.append(data.interpolantName)
				.append(" o = ")
				.append(data.vertexMain.call("v", "i"))
				.append(";\n");

		Template.assignFields(template, data.interpolant, "v2f_", "o.");

		template.append('}');
	}

	public void fragmentFooter(StringBuilder template, SourceFile file) {
		InstancingProgramMetaData data = getMetadata(file);

		Template.prefixFields(template, data.interpolant, "in", "v2f_");

		template.append("void main() {\n");
		template.append(data.interpolantName)
				.append(" o;\n");
		Template.assignFields(template, data.interpolant, "o.", "v2f_");

		template.append(data.fragmentMain.call("o"))
				.append(";\n");

		template.append('}');
	}
}
