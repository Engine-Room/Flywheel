package com.jozufozu.flywheel.core.shader;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.backend.ShaderContext;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

/**
 * A shader program that be arbitrarily "extended". This class can take in any number of program extensions, and
 * will initialize them and then call their {@link ExtensionInstance#bind() bind} function every subsequent time this
 * program is bound. An "extension" is something that interacts with the shader program in a way that is invisible to
 * the caller using the program. This is used by some programs to implement the different fog modes. Other uses might
 * include binding extra textures to allow for blocks to have normal maps, for example. As the extensions are
 * per-program, this also allows for same extra specialization within a
 * {@link ShaderContext ShaderContext}.
 */
public class ExtensibleGlProgram extends GlProgram {

	protected final List<ExtensionInstance> extensions = new ArrayList<>();

	public ExtensibleGlProgram(ResourceLocation name, int handle) {
		super(name, handle);
	}

	@Override
	public void bind() {
		super.bind();

		extensions.forEach(ExtensionInstance::bind);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("program ")
				.append(name)
				.append('[');

		for (ExtensionInstance extension : extensions) {
			builder.append(extension)
					.append('+');
		}

		builder.append(']');

		return builder.toString();
	}

	/**
	 * A factory interface to create {@link GlProgram}s parameterized by a list of extensions. This doesn't necessarily
	 * have to return an {@link ExtensibleGlProgram} if implementors want more flexibility for whatever reason.
	 */
	public interface Factory<P extends GlProgram> {

		@Nonnull
		P create(ResourceLocation name, int handle);
	}
}
