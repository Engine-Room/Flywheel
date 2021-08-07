package com.jozufozu.flywheel.core.shader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.jozufozu.flywheel.backend.gl.shader.GlProgram;
import com.jozufozu.flywheel.core.shader.extension.IExtensionInstance;
import com.jozufozu.flywheel.core.shader.extension.IProgramExtension;

import net.minecraft.util.ResourceLocation;

/**
 * A shader program that be arbitrarily "extended". This class can take in any number of program extensions, and
 * will initialize them and then call their {@link IExtensionInstance#bind() bind} function every subsequent time this
 * program is bound. An "extension" is something that interacts with the shader program in a way that is invisible to
 * the caller using the program. This is used by some programs to implement the different fog modes. Other uses might
 * include binding extra textures to allow for blocks to have normal maps, for example. As the extensions are
 * per-program, this also allows for same extra specialization within a
 * {@link com.jozufozu.flywheel.backend.ShaderContext ShaderContext}.
 */
public class ExtensibleGlProgram extends GlProgram {

	protected final List<IExtensionInstance> extensions;

	public ExtensibleGlProgram(ResourceLocation name, int handle, @Nullable List<IProgramExtension> extensions) {
		super(name, handle);

		if (extensions != null) {
			List<IExtensionInstance> list = new ArrayList<>();
			for (IProgramExtension e : extensions) {
				IExtensionInstance extension = e.create(this);
				list.add(extension);
			}
			this.extensions = list;
		} else {
			this.extensions = Collections.emptyList();
		}
	}

	@Override
	public void bind() {
		super.bind();

		extensions.forEach(IExtensionInstance::bind);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("program ")
				.append(name)
				.append('[');

		for (IExtensionInstance extension : extensions) {
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
		P create(ResourceLocation name, int handle, @Nullable List<IProgramExtension> extensions);
	}
}
