package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.vertex.VertexType;

import net.minecraft.resources.ResourceLocation;

public class ComponentRegistry {
	private static final Registry<ShaderUniforms> uniformProviders = new Registry<>();

	public static final MaterialRegistry materials = new MaterialRegistry();
	public static final Set<StructType<?>> structTypes = new HashSet<>();
	public static final Set<VertexType> vertexTypes = new HashSet<>();
	public static final Set<Context> contextShaders = new HashSet<>();

	// TODO: fill out the rest of the registry

	public static <T extends Material> T register(T material) {
		return materials.add(material);
	}

	public static <T extends StructType<?>> T register(T type) {
		structTypes.add(type);
		return type;
	}

	public static <T extends VertexType> T register(T vertexType) {
		vertexTypes.add(vertexType);
		return vertexType;
	}

	public static <T extends Context> T register(T contextShader) {
		contextShaders.add(contextShader);
		return contextShader;
	}

	public static <T extends ShaderUniforms> T register(T provider) {
		return uniformProviders.register(provider.uniformShader(), provider);
	}

	public static Collection<ShaderUniforms> getAllUniformProviders() {
		return Collections.unmodifiableCollection(uniformProviders.objects);
	}

	@Nullable
	public static ShaderUniforms getUniformProvider(ResourceLocation loc) {
		return uniformProviders.get(loc);
	}

	private static class Registry<T> {
		private final Map<ResourceLocation, T> files = new HashMap<>();
		private final List<T> objects = new ArrayList<>();

		public <O extends T> O register(ResourceLocation loc, O object) {
			if (files.containsKey(loc)) {
				throw new IllegalArgumentException("Shader file already registered: " + loc);
			}
			files.put(loc, object);
			objects.add(object);
			return object;
		}

		@Nullable
		public T get(ResourceLocation loc) {
			return files.get(loc);
		}
	}

	public static class MaterialRegistry {

		private final Set<Material> materials = new HashSet<>();
		private final MaterialSources vertexSources = new MaterialSources();
		private final MaterialSources fragmentSources = new MaterialSources();

		public <T extends Material> T add(T material) {
			materials.add(material);

			vertexSources.register(material.vertexShader());
			fragmentSources.register(material.fragmentShader());

			return material;
		}

		/**
		 * @return a list of vertex shader sources where the index in the list is the shader's ID.
		 */
		public List<ResourceLocation> vertexSources() {
			return vertexSources.sourceView;
		}

		/**
		 * @return a list of fragment shader sources where the index in the list is the shader's ID.
		 */
		public List<ResourceLocation> fragmentSources() {
			return fragmentSources.sourceView;
		}

		public int getVertexID(Material material) {
			return vertexSources.orderedSources.indexOf(material.vertexShader());
		}

		public int getFragmentID(Material material) {
			return fragmentSources.orderedSources.indexOf(material.fragmentShader());
		}

		private static class MaterialSources {
			private final Set<ResourceLocation> registered = new HashSet<>();
			private final List<ResourceLocation> orderedSources = new ArrayList<>();
			private final List<ResourceLocation> sourceView = Collections.unmodifiableList(orderedSources);

			public void register(ResourceLocation vertexShader) {
				if (registered.add(vertexShader)) {
					orderedSources.add(vertexShader);
				}
			}
		}
	}
}
