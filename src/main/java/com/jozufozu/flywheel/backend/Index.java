package com.jozufozu.flywheel.backend;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;

import net.minecraft.util.ResourceLocation;

/**
 * Indexes many shader source definitions to allow for error fix suggestions.
 */
public class Index {

	private final Multimap<String, ShaderStruct> knownNames = MultimapBuilder.hashKeys().hashSetValues().build();

	public Index(Map<ResourceLocation, SourceFile> sources) {
		Collection<SourceFile> files = sources.values();

		for (SourceFile file : files) {
			file.getStructs().forEach(knownNames::put);

		}
	}

	public Collection<ShaderStruct> getStructDefinitionsMatching(CharSequence name) {
		return knownNames.get(name.toString());
	}
}
