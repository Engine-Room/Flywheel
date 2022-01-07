package com.jozufozu.flywheel.core.shader;

import net.minecraft.resources.ResourceLocation;

public interface ExtensionInstance {

	/**
	 * Bind the extra program state. It is recommended to grab the state information from global variables.
	 */
	void bind();

	ResourceLocation name();
}
