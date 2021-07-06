package com.jozufozu.flywheel.backend.pipeline.parse;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class ErrorReporter {


	public String generateSpanError(Span span, String message) {
		SourceFile file = span.getSourceFile();

		return "";
	}
}
