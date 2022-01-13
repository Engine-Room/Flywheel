package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.source.error.ErrorBuilder;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;

import net.minecraft.resources.ResourceLocation;

public class FileIndexImpl implements FileIndex {
	public final List<SourceFile> files = new ArrayList<>();

	/**
	 * Returns an arbitrary file ID for use this compilation context, or generates one if missing.
	 * @param sourceFile The file to retrieve the ID for.
	 * @return A file ID unique to the given sourceFile.
	 */
	@Override
	public int getFileID(SourceFile sourceFile) {
		int i = files.indexOf(sourceFile);
		if (i != -1) {
			return i;
		}

		int size = files.size();
		files.add(sourceFile);
		return size;
	}

	@Override
	public SourceFile getFile(int fileId) {
		return files.get(fileId);
	}


	public void printShaderInfoLog(String source, String log, ResourceLocation name) {
		List<String> lines = log.lines()
				.toList();

		boolean needsSourceDump = false;

		StringBuilder errors = new StringBuilder();
		for (String line : lines) {
			ErrorBuilder builder = parseCompilerError(line);

			if (builder != null) {
				errors.append(builder.build());
			} else {
				errors.append(line).append('\n');
				needsSourceDump = true;
			}
		}
		Backend.LOGGER.error("Errors compiling '" + name + "': \n" + errors);
		if (needsSourceDump) {
			// TODO: generated code gets its own "file"
			ErrorReporter.printLines(source);
		}
	}

	@Nullable
	private ErrorBuilder parseCompilerError(String line) {
		try {
			ErrorBuilder error = ErrorBuilder.fromLogLine(this, line);
			if (error != null) {
				return error;
			}
		} catch (Exception ignored) {
		}

		return null;
	}
}
