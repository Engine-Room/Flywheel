package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.span.Span;

public class FileIndex {
	public final List<SourceFile> files = new ArrayList<>();

	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	public int getFileID(SourceFile sourceFile) {
		int i = files.indexOf(sourceFile);
		if (i != -1) {
			return i;
		}

		int size = files.size();
		files.add(sourceFile);
		return size;
	}

	public boolean exists(SourceFile sourceFile) {
		return files.contains(sourceFile);
	}

	public SourceFile getFile(int fileId) {
		return files.get(fileId);
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

	public Span getLineSpan(int fileId, int lineNo) {
		return getFile(fileId).getLineSpanNoWhitespace(lineNo);
	}
}
