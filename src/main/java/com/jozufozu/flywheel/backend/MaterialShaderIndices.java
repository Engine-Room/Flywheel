package com.jozufozu.flywheel.backend;

import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.material.MaterialShaders;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.resources.ResourceLocation;

public final class MaterialShaderIndices {
	private static Object2IntMap<ResourceLocation> vertexShaderIndices;
	private static Object2IntMap<ResourceLocation> fragmentShaderIndices;
	private static ObjectList<ResourceLocation> vertexShadersByIndex;
	private static ObjectList<ResourceLocation> fragmentShadersByIndex;
	private static boolean initialized;

	private MaterialShaderIndices() {
	}

	public static int getVertexShaderIndex(ResourceLocation vertexShader) {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return vertexShaderIndices.getInt(vertexShader);
	}

	public static int getFragmentShaderIndex(ResourceLocation fragmentShader) {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return fragmentShaderIndices.getInt(fragmentShader);
	}

	public static int getVertexShaderIndex(MaterialShaders shaders) {
		return getVertexShaderIndex(shaders.vertexShader());
	}

	public static int getFragmentShaderIndex(MaterialShaders shaders) {
		return getFragmentShaderIndex(shaders.fragmentShader());
	}

	public static ResourceLocation getVertexShader(int index) {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return vertexShadersByIndex.get(index);
	}

	public static ResourceLocation getFragmentShader(int index) {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return fragmentShadersByIndex.get(index);
	}

	@Unmodifiable
	public static List<ResourceLocation> getAllVertexShaders() {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return vertexShadersByIndex;
	}

	@Unmodifiable
	public static List<ResourceLocation> getAllFragmentShaders() {
		if (!initialized) {
			throw new IllegalStateException("Not initialized!");
		}
		return fragmentShadersByIndex;
	}

	private static void initInner() {
		int amount = MaterialShaders.REGISTRY.getAll().size();

		Object2IntMap<ResourceLocation> vertexShaderIndices = new Object2IntOpenHashMap<>();
		vertexShaderIndices.defaultReturnValue(-1);
		Object2IntMap<ResourceLocation> fragmentShaderIndices = new Object2IntOpenHashMap<>();
		fragmentShaderIndices.defaultReturnValue(-1);
		ObjectList<ResourceLocation> vertexShadersByIndex = new ObjectArrayList<>(amount);
		ObjectList<ResourceLocation> fragmentShadersByIndex = new ObjectArrayList<>(amount);

		ObjectSet<ResourceLocation> allVertexShaders = new ObjectOpenHashSet<>();
		ObjectSet<ResourceLocation> allFragmentShaders = new ObjectOpenHashSet<>();

		int vertexShaderIndex = 0;
		int fragmentShaderIndex = 0;
		for (MaterialShaders shaders : MaterialShaders.REGISTRY) {
			ResourceLocation vertexShader = shaders.vertexShader();
			if (allVertexShaders.add(vertexShader)) {
				vertexShaderIndices.put(vertexShader, vertexShaderIndex);
				vertexShadersByIndex.add(vertexShader);
				vertexShaderIndex++;
			}
			ResourceLocation fragmentShader = shaders.fragmentShader();
			if (allFragmentShaders.add(fragmentShader)) {
				fragmentShaderIndices.put(fragmentShader, fragmentShaderIndex);
				fragmentShadersByIndex.add(fragmentShader);
				fragmentShaderIndex++;
			}
		}

		MaterialShaderIndices.vertexShaderIndices = Object2IntMaps.unmodifiable(vertexShaderIndices);
		MaterialShaderIndices.fragmentShaderIndices = Object2IntMaps.unmodifiable(fragmentShaderIndices);
		MaterialShaderIndices.vertexShadersByIndex = ObjectLists.unmodifiable(vertexShadersByIndex);
		MaterialShaderIndices.fragmentShadersByIndex = ObjectLists.unmodifiable(fragmentShadersByIndex);

		initialized = true;
	}

	public static void init() {
		MaterialShaders.REGISTRY.addFreezeCallback(MaterialShaderIndices::initInner);
	}
}
