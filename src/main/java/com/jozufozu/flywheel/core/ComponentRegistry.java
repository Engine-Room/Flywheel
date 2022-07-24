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

import net.minecraft.resources.ResourceLocation;

public class ComponentRegistry {
	private static final Registry<UniformProvider> uniformProviders = new Registry<>();

	public static final Set<Material> materials = new HashSet<>();
	public static final Set<StructType<?>> structTypes = new HashSet<>();
	public static final Set<VertexType> vertexTypes = new HashSet<>();
	public static final Set<ContextShader> contextShaders = new HashSet<>();

	// TODO: fill out the rest of the registry

	public static <T extends Material> T register(T material) {
		materials.add(material);
		return material;
	}

	public static <T extends StructType<?>> T register(T type) {
		structTypes.add(type);
		return type;
	}

	public static <T extends VertexType> T register(T vertexType) {
		vertexTypes.add(vertexType);
		return vertexType;
	}

	public static ContextShader register(ContextShader contextShader) {
		contextShaders.add(contextShader);
		return contextShader;
	}

	public static <T extends UniformProvider> T register(T provider) {
		return uniformProviders.register(provider.getUniformShader()
				.getFileLoc(), provider);
	}

	public static Collection<UniformProvider> getAllUniformProviders() {
		return Collections.unmodifiableCollection(uniformProviders.objects);
	}

	private static class Registry<T> {
		private final Set<ResourceLocation> files = new HashSet<>();
		private final List<T> objects = new ArrayList<>();

		public <O extends T> O register(ResourceLocation loc, O object) {
			if (files.contains(loc)) {
				throw new IllegalArgumentException("Shader file already registered: " + loc);
			}
			files.add(loc);
			objects.add(object);
			return object;
		}
	}
}
