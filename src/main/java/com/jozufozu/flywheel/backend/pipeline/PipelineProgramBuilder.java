package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PipelineProgramBuilder {

	private final List<SourceFile> sources = new ArrayList<>();

	public PipelineProgramBuilder() {

	}

	public PipelineProgramBuilder include(SourceFile file) {
		sources.add(file);
		return this;
	}

	public PipelineProgramBuilder includeAll(Collection<? extends SourceFile> files) {
		sources.addAll(files);
		return this;
	}
}
