package com.jozufozu.flywheel.core.compile;

import com.jozufozu.flywheel.backend.source.SourceFile;

public interface FileIndex {
	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 *
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	int getFileID(SourceFile sourceFile);
}
