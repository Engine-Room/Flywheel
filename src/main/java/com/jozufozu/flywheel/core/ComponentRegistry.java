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

import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.core.source.FileResolution;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public class ComponentRegistry {
	private static final Registry<UniformProvider> uniformProviders = new Registry<>();

	public static final MaterialRegistry materials = new MaterialRegistry();
	public static final Set<StructType<?>> structTypes = new HashSet<>();
	public static final Set<VertexType> vertexTypes = new HashSet<>();
	public static final Set<ContextShader> contextShaders = new HashSet<>();

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

	public static ContextShader register(ContextShader contextShader) {
		contextShaders.add(contextShader);
		return contextShader;
	}

	public static <T extends UniformProvider> T register(T provider) {
		return uniformProviders.register(provider.uniformShader()
				.getFileLoc(), provider);
	}

	public static Collection<UniformProvider> getAllUniformProviders() {
		return Collections.unmodifiableCollection(uniformProviders.objects);
	}

	@Nullable
	public static UniformProvider getUniformProvider(ResourceLocation loc) {
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

			vertexSources.register(material, material.getVertexShader());
			fragmentSources.register(material, material.getFragmentShader());

			return material;
		}

		/**
		 * @return a list of vertex shader sources where the index in the list is the shader's ID.
		 */
		public List<FileResolution> vertexSources() {
			return vertexSources.sourceView;
		}

		/**
		 * @return a list of fragment shader sources where the index in the list is the shader's ID.
		 */
		public List<FileResolution> fragmentSources() {
			return fragmentSources.sourceView;
		}

		private static class MaterialSources {
			private final Set<FileResolution> registered = new HashSet<>();
			private final List<FileResolution> orderedSources = new ArrayList<>();
			private final Reference2IntMap<Material> material2ID = new Reference2IntOpenHashMap<>();
			private final List<FileResolution> sourceView = Collections.unmodifiableList(orderedSources);

			public void register(Material material, FileResolution vertexShader) {
				if (registered.add(vertexShader)) {
					material2ID.put(material, orderedSources.size());
					orderedSources.add(vertexShader);
				}
			}
		}
	}
}
