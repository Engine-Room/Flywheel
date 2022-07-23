package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.compile.ContextShader;
import com.jozufozu.flywheel.core.vertex.BlockVertex;

import net.minecraft.resources.ResourceLocation;

public class ComponentRegistry {

	private static final Set<ResourceLocation> uniformProviderFiles = new HashSet<>();
	private static final List<UniformProvider> uniformProviders = new ArrayList<>();

	// TODO: fill out the rest of the registry

	public static <T extends Material> T register(T material) {
		return material;
	}

	public static <T extends StructType<?>> T register(T type) {

		return type;
	}

	public static <T extends VertexType> T register(T vertexType) {
		return vertexType;
	}

	public static ContextShader register(ContextShader contextShader) {
		return contextShader;
	}

	public static <T extends UniformProvider> T register(T provider) {

		var file = provider.getUniformShader();

		ResourceLocation location = file.getFileLoc();
		if (uniformProviderFiles.contains(location)) {
			throw new IllegalArgumentException("UniformProvider for '" + location + "' already registered");
		}

		uniformProviderFiles.add(location);
		uniformProviders.add(provider);
		return provider;
	}

	public static Collection<UniformProvider> getAllUniformProviders() {
		return Collections.unmodifiableCollection(uniformProviders);
	}
}
