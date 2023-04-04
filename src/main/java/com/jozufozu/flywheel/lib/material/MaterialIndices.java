package com.jozufozu.flywheel.lib.material;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.material.Material;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;

// TODO: add messages to exceptions
public class MaterialIndices {
	private static Reference2IntMap<Material> materialIndices;
	private static Object2IntMap<ResourceLocation> vertexShaderIndices;
	private static Object2IntMap<ResourceLocation> fragmentShaderIndices;
	private static ObjectList<Material> materialsByIndex;
	private static ObjectList<ResourceLocation> vertexShadersByIndex;
	private static ObjectList<ResourceLocation> fragmentShadersByIndex;
	private static boolean initialized;

	public static int getMaterialIndex(Material material) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return materialIndices.getInt(material);
	}

	public static int getVertexShaderIndex(ResourceLocation vertexShader) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return vertexShaderIndices.getInt(vertexShader);
	}

	public static int getFragmentShaderIndex(ResourceLocation fragmentShader) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return fragmentShaderIndices.getInt(fragmentShader);
	}

	public static int getVertexShaderIndex(Material material) {
		return getVertexShaderIndex(material.vertexShader());
	}

	public static int getFragmentShaderIndex(Material material) {
		return getFragmentShaderIndex(material.fragmentShader());
	}

	public static Material getMaterial(int index) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return materialsByIndex.get(index);
	}

	public static ResourceLocation getVertexShader(int index) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return vertexShadersByIndex.get(index);
	}

	public static ResourceLocation getFragmentShader(int index) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return fragmentShadersByIndex.get(index);
	}

	@Unmodifiable
	public static List<Material> getAllMaterials() {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return materialsByIndex;
	}

	@Unmodifiable
	public static List<ResourceLocation> getAllVertexShaders() {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return vertexShadersByIndex;
	}

	@Unmodifiable
	public static List<ResourceLocation> getAllFragmentShaders() {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return fragmentShadersByIndex;
	}

	private static void initInner() {
		int amount = Material.REGISTRY.getAll().size();

		Reference2IntMap<Material> materialIndices = new Reference2IntOpenHashMap<>();
		Object2IntMap<ResourceLocation> vertexShaderIndices = new Object2IntOpenHashMap<>();
		Object2IntMap<ResourceLocation> fragmentShaderIndices = new Object2IntOpenHashMap<>();
		ObjectList<Material> materialsByIndex = new ObjectArrayList<>(amount);
		ObjectList<ResourceLocation> vertexShadersByIndex = new ObjectArrayList<>(amount);
		ObjectList<ResourceLocation> fragmentShadersByIndex = new ObjectArrayList<>(amount);

		ObjectSet<ResourceLocation> allVertexShaders = new ObjectOpenHashSet<>();
		ObjectSet<ResourceLocation> allFragmentShaders = new ObjectOpenHashSet<>();

		int materialIndex = 0;
		int vertexShaderIndex = 0;
		int fragmentShaderIndex = 0;
		for (Material material : Material.REGISTRY) {
			materialIndices.put(material, materialIndex);
			materialsByIndex.add(material);
			materialIndex++;
			ResourceLocation vertexShader = material.vertexShader();
			if (allVertexShaders.add(vertexShader)) {
				vertexShaderIndices.put(vertexShader, vertexShaderIndex);
				vertexShadersByIndex.add(vertexShader);
				vertexShaderIndex++;
			}
			ResourceLocation fragmentShader = material.fragmentShader();
			if (allFragmentShaders.add(fragmentShader)) {
				fragmentShaderIndices.put(fragmentShader, fragmentShaderIndex);
				fragmentShadersByIndex.add(fragmentShader);
				fragmentShaderIndex++;
			}
		}

		MaterialIndices.materialIndices = Reference2IntMaps.unmodifiable(materialIndices);
		MaterialIndices.vertexShaderIndices = Object2IntMaps.unmodifiable(vertexShaderIndices);
		MaterialIndices.fragmentShaderIndices = Object2IntMaps.unmodifiable(fragmentShaderIndices);
		MaterialIndices.materialsByIndex = ObjectLists.unmodifiable(materialsByIndex);
		MaterialIndices.vertexShadersByIndex = ObjectLists.unmodifiable(vertexShadersByIndex);
		MaterialIndices.fragmentShadersByIndex = ObjectLists.unmodifiable(fragmentShadersByIndex);

		initialized = true;
	}

	@ApiStatus.Internal
	public static void init() {
		Material.REGISTRY.addFreezeCallback(MaterialIndices::initInner);
	}
}
