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
public class MaterialIndicies {
	private static Reference2IntMap<Material> materialIndicies;
	private static Object2IntMap<ResourceLocation> vertexShaderIndicies;
	private static Object2IntMap<ResourceLocation> fragmentShaderIndicies;
	private static ObjectList<Material> materialsByIndex;
	private static ObjectList<ResourceLocation> vertexShadersByIndex;
	private static ObjectList<ResourceLocation> fragmentShadersByIndex;
	private static boolean initialized;

	public static int getMaterialIndex(Material material) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return materialIndicies.getInt(material);
	}

	public static int getVertexShaderIndex(ResourceLocation vertexShader) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return vertexShaderIndicies.getInt(vertexShader);
	}

	public static int getFragmentShaderIndex(ResourceLocation fragmentShader) {
		if (!initialized) {
			throw new IllegalStateException();
		}
		return fragmentShaderIndicies.getInt(fragmentShader);
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

		Reference2IntMap<Material> materialIndicies = new Reference2IntOpenHashMap<>();
		Object2IntMap<ResourceLocation> vertexShaderIndicies = new Object2IntOpenHashMap<>();
		Object2IntMap<ResourceLocation> fragmentShaderIndicies = new Object2IntOpenHashMap<>();
		ObjectList<Material> materialsByIndex = new ObjectArrayList<>(amount);
		ObjectList<ResourceLocation> vertexShadersByIndex = new ObjectArrayList<>(amount);
		ObjectList<ResourceLocation> fragmentShadersByIndex = new ObjectArrayList<>(amount);

		ObjectSet<ResourceLocation> allVertexShaders = new ObjectOpenHashSet<>();
		ObjectSet<ResourceLocation> allFragmentShaders = new ObjectOpenHashSet<>();

		int materialIndex = 0;
		int vertexShaderIndex = 0;
		int fragmentShaderIndex = 0;
		for (Material material : Material.REGISTRY) {
			materialIndicies.put(material, materialIndex);
			materialsByIndex.add(material);
			materialIndex++;
			ResourceLocation vertexShader = material.vertexShader();
			if (allVertexShaders.add(vertexShader)) {
				vertexShaderIndicies.put(vertexShader, vertexShaderIndex);
				vertexShadersByIndex.add(vertexShader);
				vertexShaderIndex++;
			}
			ResourceLocation fragmentShader = material.fragmentShader();
			if (allFragmentShaders.add(fragmentShader)) {
				fragmentShaderIndicies.put(fragmentShader, fragmentShaderIndex);
				fragmentShadersByIndex.add(fragmentShader);
				fragmentShaderIndex++;
			}
		}

		MaterialIndicies.materialIndicies = Reference2IntMaps.unmodifiable(materialIndicies);
		MaterialIndicies.vertexShaderIndicies = Object2IntMaps.unmodifiable(vertexShaderIndicies);
		MaterialIndicies.fragmentShaderIndicies = Object2IntMaps.unmodifiable(fragmentShaderIndicies);
		MaterialIndicies.materialsByIndex = ObjectLists.unmodifiable(materialsByIndex);
		MaterialIndicies.vertexShadersByIndex = ObjectLists.unmodifiable(vertexShadersByIndex);
		MaterialIndicies.fragmentShadersByIndex = ObjectLists.unmodifiable(fragmentShadersByIndex);

		initialized = true;
	}

	@ApiStatus.Internal
	public static void init() {
		Material.REGISTRY.addFreezeCallback(MaterialIndicies::initInner);
	}
}
