package com.jozufozu.flywheel.backend.pipeline;

import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ProtoProgram;

public class InstancingTemplate implements ITemplate {

	public static final InstancingTemplate INSTANCE = new InstancingTemplate();

	private final Map<SourceFile, InstanceTemplateData> datas = new HashMap<>();

	@Override
	public void generateTemplateSource(StringBuilder builder, ShaderType type, SourceFile file) {
		if (type == ShaderType.VERTEX) {
			vertexFooter(builder, file);
		} else if (type == ShaderType.FRAGMENT) {
			fragmentFooter(builder, file);
		}
	}

	@Override
	public void attachAttributes(ProtoProgram program, SourceFile file) {
		InstanceTemplateData data = getData(file);
		data.vertex.addPrefixedAttributes(program, "a_v_");
		data.instance.addPrefixedAttributes(program, "a_i_");
	}

	public InstanceTemplateData getData(SourceFile file) {
		return datas.computeIfAbsent(file, InstanceTemplateData::new);
	}

	public void vertexFooter(StringBuilder template, SourceFile file) {
		InstanceTemplateData data = getData(file);

		ITemplate.prefixFields(template, data.vertex, "attribute", "a_v_");
		ITemplate.prefixFields(template, data.instance, "attribute", "a_i_");
		ITemplate.prefixFields(template, data.interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(data.vertexName)
				.append(" v;\n");
		ITemplate.assignFields(template, data.vertex, "v.", "a_v_");

		template.append(data.instanceName)
				.append(" i;\n");
		ITemplate.assignFields(template, data.instance, "i.", "a_i_");

		template.append(data.interpolantName)
				.append(" o = ")
				.append(data.vertexMain.call("v", "i"))
				.append(";\n");

		ITemplate.assignFields(template, data.interpolant, "v2f_", "o.");

		template.append('}');
	}

	public void fragmentFooter(StringBuilder template, SourceFile file) {
		InstanceTemplateData data = getData(file);

		ITemplate.prefixFields(template, data.interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(data.interpolantName)
				.append(" o;\n");
		ITemplate.assignFields(template, data.interpolant, "o.", "v2f_");

		template.append(data.fragmentMain.call("o"))
				.append(";\n");

		template.append('}');
	}
}
