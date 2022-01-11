package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.gl.shader.GlShader;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.source.FileResolution;
import com.jozufozu.flywheel.backend.source.SourceFile;
import com.jozufozu.flywheel.core.shader.ProgramSpec;

public class FragmentCompiler extends Memoizer<ProgramContext, GlShader> {
	private final FileResolution header;
	private final Template<FragmentTemplateData> fragment;

	public FragmentCompiler(Template<FragmentTemplateData> fragment, FileResolution header) {
		this.header = header;
		this.fragment = fragment;
	}

	@Override
	protected GlShader _create(ProgramContext key) {
		ProgramSpec spec = key.spec();
		SourceFile fragmentFile = spec.getFragmentFile();
		FragmentTemplateData appliedTemplate = fragment.apply(fragmentFile);

		StringBuilder builder = new StringBuilder();

		builder.append(CompileUtil.generateHeader(fragment.getVersion(), ShaderType.FRAGMENT));

		key.getShaderConstants().writeInto(builder);

		FileIndexImpl index = new FileIndexImpl();

		header.getFile().generateFinalSource(index, builder);
		fragmentFile.generateFinalSource(index, builder);

		builder.append(appliedTemplate.generateFooter());

		return new GlShader(spec.name, ShaderType.FRAGMENT, builder.toString());
	}

	@Override
	protected void _destroy(GlShader value) {
		value.delete();
	}
}
