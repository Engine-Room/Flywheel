package com.jozufozu.flywheel.backend.pipeline.error;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class ErrorBuilder {

	private StringBuilder internal;

	public ErrorBuilder header(CharSequence msg) {
		internal.append("error: ")
				.append(msg);
		return endLine();
	}

	public ErrorBuilder errorIn(SourceFile file) {
		internal.append("--> ")
				.append(file.name);
		return endLine();
	}

	public ErrorBuilder line(int no, CharSequence content) {

		return endLine();
	}

	public ErrorBuilder endLine() {
		internal.append('\n');
		return this;
	}
}
