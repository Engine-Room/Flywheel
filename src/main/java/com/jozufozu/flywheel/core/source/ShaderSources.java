package com.jozufozu.flywheel.core.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
	public static final String SHADER_DIR = "flywheel/shaders/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");

	private final Map<ResourceLocation, SourceFile> shaderSources = new HashMap<>();

	public final Index index;

	public ShaderSources(ResourceManager manager) {
		Collection<ResourceLocation> allShaders = manager.listResources(SHADER_DIR, s -> {
			for (String ext : EXTENSIONS) {
				if (s.endsWith(ext)) return true;
			}
			return false;
		});

		for (ResourceLocation location : allShaders) {
			try (Resource resource = manager.getResource(location)) {
				String source = StringUtil.readToString(resource.getInputStream());

				ResourceLocation name = ResourceUtil.removePrefixUnchecked(location, SHADER_DIR);

				shaderSources.put(name, new SourceFile(this, name, source));
			} catch (IOException e) {
				//
			}
		}

		index = new Index(shaderSources);
	}

	@Override
	@Nullable
	public SourceFile findSource(ResourceLocation name) {

		return shaderSources.get(name);
	}
}
