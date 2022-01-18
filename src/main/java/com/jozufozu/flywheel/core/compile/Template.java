package com.jozufozu.flywheel.core.compile;

import java.util.function.Function;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.source.SourceFile;

/**
 * A class that generates glsl glue code given a SourceFile.
 *
 * <p>
 *     Shader files are written somewhat abstractly. Subclasses of Template handle those abstractions, using SourceFile
 *     metadata to generate shader code that OpenGL can use to call into our shader programs.
 * </p>
 */
public class Template<T> extends Memoizer<SourceFile, T> {

	private final Function<SourceFile, T> reader;
	private final GLSLVersion glslVersion;

	public Template(GLSLVersion glslVersion, Function<SourceFile, T> reader) {
		this.reader = reader;
		this.glslVersion = glslVersion;
	}

	/**
	 * Verify that the given SourceFile is valid for this Template and return the metadata.
	 * @param file The SourceFile to apply this Template to.
	 * @return The applied template metadata.
	 */
	public T apply(SourceFile file) {
		// lazily read files, cache results
		return super.get(file);
	}

	/**
	 * @return The GLSL version this template requires.
	 */
	public GLSLVersion getVersion() {
		return glslVersion;
	}

	@Override
	protected T _create(SourceFile key) {
		return reader.apply(key);
	}

	@Override
	protected void _destroy(T value) {
		// noop
	}
}
