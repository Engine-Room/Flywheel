package com.jozufozu.flywheel.backend.pipeline.error;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class ErrorReporter {

	public static void generateSpanError(Span span, String message) {
		SourceFile file = span.getSourceFile();

		ErrorBuilder builder = new ErrorBuilder();

		CharSequence error = builder.header(message)
				.errorIn(file)
				.pointAt(span, 2)
				.build();

		Backend.log.info(error);
	}
}
