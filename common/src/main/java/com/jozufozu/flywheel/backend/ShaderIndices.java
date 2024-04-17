package com.jozufozu.flywheel.backend;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.material.CutoutShader;
import com.jozufozu.flywheel.api.material.FogShader;
import com.jozufozu.flywheel.api.material.MaterialShaders;
import com.jozufozu.flywheel.api.registry.Registry;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.resources.ResourceLocation;

public final class ShaderIndices {
	@Nullable
	private static Index vertexShaders;
	@Nullable
	private static Index fragmentShaders;
	@Nullable
	private static Index fogShaders;
	@Nullable
	private static Index cutoutShaders;

	private ShaderIndices() {
	}

	public static Index materialVertex() {
		if (vertexShaders == null) {
			throw new IllegalStateException("Not initialized!");
		}
		return vertexShaders;
	}

	public static Index materialFragment() {
		if (fragmentShaders == null) {
			throw new IllegalStateException("Not initialized!");
		}
		return fragmentShaders;
	}

	public static Index fog() {
		if (fogShaders == null) {
			throw new IllegalStateException("Not initialized!");
		}
		return fogShaders;
	}

	public static Index cutout() {
		if (cutoutShaders == null) {
			throw new IllegalStateException("Not initialized!");
		}
		return cutoutShaders;
	}

	public static int getVertexShaderIndex(MaterialShaders shaders) {
		return materialVertex().index(shaders.vertexShader());
	}

	public static int getFragmentShaderIndex(MaterialShaders shaders) {
		return materialFragment().index(shaders.fragmentShader());
	}

	public static int getFogShaderIndex(FogShader fogShader) {
		return fog().index(fogShader.source());
	}

	public static int getCutoutShaderIndex(CutoutShader cutoutShader) {
		return cutout().index(cutoutShader.source());
	}

	private static void initMaterialShaders(Registry<MaterialShaders> registry) {
		int amount = registry.getAll()
				.size();

		var vertexShaders = new IndexBuilder(amount);
		var fragmentShaders = new IndexBuilder(amount);

		for (MaterialShaders shaders : registry) {
			vertexShaders.add(shaders.vertexShader());
			fragmentShaders.add(shaders.fragmentShader());
		}

		ShaderIndices.vertexShaders = vertexShaders.build();
		ShaderIndices.fragmentShaders = fragmentShaders.build();
	}

	private static void initFogShaders(Registry<FogShader> registry) {
		int amount = registry.getAll()
				.size();

		var fog = new IndexBuilder(amount);

		for (FogShader shaders : registry) {
			fog.add(shaders.source());
		}

		ShaderIndices.fogShaders = fog.build();
	}

	private static void initCutoutShaders(Registry<CutoutShader> registry) {
		int amount = registry.getAll()
				.size();

		var cutout = new IndexBuilder(amount);

		for (CutoutShader shaders : registry) {
			cutout.add(shaders.source());
		}

		ShaderIndices.cutoutShaders = cutout.build();
	}

	public static void init() {
		MaterialShaders.REGISTRY.addFreezeCallback(ShaderIndices::initMaterialShaders);
		FogShader.REGISTRY.addFreezeCallback(ShaderIndices::initFogShaders);
		CutoutShader.REGISTRY.addFreezeCallback(ShaderIndices::initCutoutShaders);
	}

	public static class Index {
		private final Object2IntMap<ResourceLocation> shaders2Index;
		private final ObjectList<ResourceLocation> shaders;

		private Index(IndexBuilder builder) {
			this.shaders2Index = Object2IntMaps.unmodifiable(builder.shaders2Index);
			this.shaders = ObjectLists.unmodifiable(builder.shaders);
		}

		public int index(ResourceLocation shader) {
			return shaders2Index.getInt(shader);
		}

		@Unmodifiable
		public List<ResourceLocation> all() {
			return shaders;
		}

		public ResourceLocation get(int index) {
			return shaders.get(index);
		}
	}

	private static class IndexBuilder {
		private int index;
		private final Object2IntMap<ResourceLocation> shaders2Index;
		private final ObjectList<ResourceLocation> shaders;

		public IndexBuilder(int amount) {
			shaders2Index = new Object2IntOpenHashMap<>();
			shaders2Index.defaultReturnValue(-1);
			shaders = new ObjectArrayList<>(amount);
		}

		public void add(ResourceLocation shader) {
			if (shaders2Index.putIfAbsent(shader, index) == -1) {
				shaders.add(shader);
				index++;
			}
		}

		public Index build() {
			return new Index(this);
		}
	}
}
