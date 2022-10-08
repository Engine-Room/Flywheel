package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.span.Span;

public class CompilationContext {
	public final List<SourceFile> files = new ArrayList<>();

	private String generatedSource = "";
	private int generatedLines = 0;


	public String sourceHeader(SourceFile sourceFile) {
		return "#line " + 0 + ' ' + getOrCreateFileID(sourceFile) + " // " + sourceFile.name + '\n';
	}

	public String generatedHeader(String generatedCode, @Nullable String comment) {
		generatedSource += generatedCode;
		int lines = generatedCode.split("\n").length;

		var out = "#line " + generatedLines + ' ' + 0;

		generatedLines += lines;

		if (comment == null) {
			comment = "";
		}

		return out + " // (generated) " + comment + '\n';
	}

	public boolean contains(SourceFile sourceFile) {
		return files.contains(sourceFile);
	}

	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 *
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	private int getOrCreateFileID(SourceFile sourceFile) {
		int i = files.indexOf(sourceFile);
		if (i != -1) {
			return i + 1;
		}

		files.add(sourceFile);
		return files.size();
	}

	public Span getLineSpan(int fileId, int lineNo) {
		if (fileId == 0) {
			// TODO: Valid spans for generated code.
			return null;
		}
		return getFile(fileId).getLineSpanNoWhitespace(lineNo);
	}

	private SourceFile getFile(int fileId) {
		return files.get(fileId - 1);
	}

	public String parseErrors(String log) {
		List<String> lines = log.lines()
				.toList();

		StringBuilder errors = new StringBuilder();
		for (String line : lines) {
			ErrorBuilder builder = parseCompilerError(line);

			if (builder != null) {
				errors.append(builder.build());
			} else {
				errors.append(line).append('\n');
			}
		}
		return errors.toString();
	}

	@Nullable
	private ErrorBuilder parseCompilerError(String line) {
		try {
			return ErrorBuilder.fromLogLine(this, line);
		} catch (Exception ignored) {
		}

		return null;
	}
}
