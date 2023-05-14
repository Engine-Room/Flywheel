package com.jozufozu.flywheel.backend.compile.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.glsl.LoadError;
import com.jozufozu.flywheel.glsl.LoadResult;
import com.jozufozu.flywheel.glsl.error.ErrorBuilder;
import com.jozufozu.flywheel.util.StringUtil;

public class CompilerStats {
	private long compileStart;

	private final Set<LoadError> loadErrors = new HashSet<>();
	private final List<FailedCompilation> shaderErrors = new ArrayList<>();
	private final List<String> programErrors = new ArrayList<>();

	private boolean errored = false;
	private int shaderCount = 0;
	private int programCount = 0;

	public void start() {
		compileStart = System.nanoTime();
	}

	public void finish() {
		long compileEnd = System.nanoTime();
		var elapsed = StringUtil.formatTime(compileEnd - compileStart);

		Flywheel.LOGGER.info("Compiled " + shaderCount + " shaders (with " + shaderErrors.size() + " compile errors) " + "and " + programCount + " programs (with " + programErrors.size() + " link errors) in " + elapsed);
	}

	public boolean errored() {
		return errored;
	}

	public String generateErrorLog() {
		String out = "";

		if (!loadErrors.isEmpty()) {
			out += "\nErrors loading sources:\n" + loadErrors();
		}

		if (!shaderErrors.isEmpty()) {
			out += "\nShader compilation errors:\n" + compileErrors();
		}

		if (!programErrors.isEmpty()) {
			out += "\nProgram link errors:\n" + linkErrors();
		}

		return out;
	}

	private String compileErrors() {
		return shaderErrors.stream()
				.map(FailedCompilation::generateMessage)
				.collect(Collectors.joining("\n"));
	}

	@NotNull
	private String linkErrors() {
		return String.join("\n", programErrors);
	}

	private String loadErrors() {
		return loadErrors.stream()
				.map(LoadError::generateMessage)
				.map(ErrorBuilder::build)
				.collect(Collectors.joining("\n"));
	}

	public void shaderResult(ShaderResult result) {
		if (result instanceof ShaderResult.Failure f) {
			shaderErrors.add(f.failure());
			errored = true;
		}
		shaderCount++;
	}

	public void linkResult(LinkResult linkResult) {
		if (linkResult instanceof LinkResult.Failure f) {
			programErrors.add(f.failure());
			errored = true;
		}
		programCount++;
	}

	public void loadResult(LoadResult loadResult) {
		if (loadResult instanceof LoadResult.Failure f) {
			loadErrors.add(f.error());
			errored = true;
		}
	}
}
