package com.jozufozu.flywheel.backend.pipeline;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.loading.ProtoProgram;

public class OneShotTemplate implements ITemplate {

	public static final OneShotTemplate INSTANCE = new OneShotTemplate();


	private final Map<SourceFile, OneShotData> datas = new HashMap<>();

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
		OneShotData data = getData(file);
		data.vertex.addPrefixedAttributes(program, "a_v_");
	}

	public OneShotData getData(SourceFile file) {
		return datas.computeIfAbsent(file, OneShotData::new);
	}

	public void vertexFooter(StringBuilder template, SourceFile file) {
		OneShotData data = getData(file);

		ITemplate.prefixFields(template, data.vertex, "attribute", "a_v_");
		ITemplate.prefixFields(template, data.interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(data.vertexName)
				.append(" v;\n");
		ITemplate.assignFields(template, data.vertex, "v.", "a_v_");

		template.append(data.interpolantName)
				.append(" o = ")
				.append(data.vertexMain.call("v"))
				.append(";\n");

		ITemplate.assignFields(template, data.interpolant, "v2f_", "o.");

		template.append('}');
	}

	public void fragmentFooter(StringBuilder template, SourceFile file) {
		OneShotData data = getData(file);

		ITemplate.prefixFields(template, data.interpolant, "varying", "v2f_");

		template.append("void main() {\n");
		template.append(data.interpolant.name)
				.append(" o;\n");
		ITemplate.assignFields(template, data.interpolant, "o.", "v2f_");

		template.append(data.fragmentMain.call("o"))
				.append(";\n");

		template.append('}');
	}
}
