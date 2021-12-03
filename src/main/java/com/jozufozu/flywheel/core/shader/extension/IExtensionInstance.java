package com.jozufozu.flywheel.core.shader.extension;

import net.minecraft.resources.ResourceLocation;

public interface IExtensionInstance {

	/**
	 * Bind the extra program state. It is recommended to grab the state information from global variables.
	 */
	void bind();

	ResourceLocation name();
}
