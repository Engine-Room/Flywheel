package com.jozufozu.flywheel.core.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.jozufozu.flywheel.util.ResourceUtil;
import com.jozufozu.flywheel.util.StringUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * The main object for loading and parsing source files.
 */
public class ShaderSources implements SourceFinder {
	public static final String SHADER_DIR = "flywheel/shaders";
	public static final String SHADER_DIR_SLASH = SHADER_DIR + "/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");

	private final Map<ResourceLocation, SourceFile> shaderSources = new HashMap<>();

	public final Index index;

	public ShaderSources(ResourceManager manager) {
		Map<ResourceLocation, Resource> allShaders = manager.listResources(SHADER_DIR, loc -> {
			String path = loc.getPath();
			for (String ext : EXTENSIONS) {
				if (path.endsWith(ext)) return true;
			}
			return false;
		});

		allShaders.forEach((location, resource) -> {
			try (InputStream inputStream = resource.open()) {
				String source = StringUtil.readToString(inputStream);

				ResourceLocation name = ResourceUtil.removePrefixUnchecked(location, SHADER_DIR_SLASH);

				shaderSources.put(name, new SourceFile(this, name, source));
			} catch (IOException ignored) {

			}
		});

		index = new Index(shaderSources);
	}

	@Override
	@Nullable
	public SourceFile findSource(ResourceLocation name) {

		return shaderSources.get(name);
	}
}
