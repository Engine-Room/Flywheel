package com.jozufozu.flywheel.core.source;

import com.jozufozu.flywheel.core.source.span.Span;

public interface FileIndex {
	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 *
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	int getFileID(SourceFile sourceFile);

	SourceFile getFile(int fileID);

	default Span getLineSpan(int fileId, int lineNo) {
		return getFile(fileId).getLineSpanNoWhitespace(lineNo);
	}
}
