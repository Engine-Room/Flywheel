package com.jozufozu.flywheel.backend.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.util.StringUtil;

public class CompilerStats {
	private long compileStart;

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

	// TODO: use this to turn off backends
	public boolean errored() {
		return errored;
	}

	private String generateLog() {
		return String.join("\n", programErrors) + '\n' + shaderErrors.stream()
				.map(FailedCompilation::getMessage)
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
}
