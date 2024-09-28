package dev.engine_room.flywheel.backend;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.api.registry.Registry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.resources.ResourceLocation;

public final class MaterialShaderIndices {
	@Nullable
	private static Index fogSources;
	@Nullable
	private static Index cutoutSources;

	private MaterialShaderIndices() {
	}

	public static Index fogSources() {
		if (fogSources == null) {
			fogSources = indexFromRegistry(FogShader.REGISTRY, FogShader::source);
		}
		return fogSources;
	}

	public static Index cutoutSources() {
		//		if (cutoutSources == null) {
		//			cutoutSources = indexFromRegistry(CutoutShader.REGISTRY, CutoutShader::source);
		//		}
		return cutoutSources;
	}

	public static int fogIndex(FogShader fogShader) {
		return fogSources().index(fogShader.source());
	}

	public static int cutoutIndex(CutoutShader cutoutShader) {
		return 0;//cutoutSources().index(cutoutShader.source());
	}

	private static <T> Index indexFromRegistry(Registry<T> registry, Function<T, ResourceLocation> sourceFunc) {
		if (!registry.isFrozen()) {
			throw new IllegalStateException("Cannot create index from registry that is not frozen!");
		}

		var builder = new IndexBuilder();

		for (T object : registry) {
			builder.add(sourceFunc.apply(object));
		}

		return builder.build();
	}

	public static class Index {
		private final Object2IntMap<ResourceLocation> sources2Index;
		private final ObjectList<ResourceLocation> sources;

		private Index(IndexBuilder builder) {
			this.sources2Index = new Object2IntOpenHashMap<>(builder.sources2Index);
			this.sources = new ObjectArrayList<>(builder.sources);
		}

		public int index(ResourceLocation source) {
			return sources2Index.getInt(source);
		}

		public ResourceLocation get(int index) {
			return sources.get(index);
		}

		@Unmodifiable
		public List<ResourceLocation> all() {
			return sources;
		}
	}

	private static class IndexBuilder {
		private final Object2IntMap<ResourceLocation> sources2Index;
		private final ObjectList<ResourceLocation> sources;
		private int index = 0;

		public IndexBuilder() {
			sources2Index = new Object2IntOpenHashMap<>();
			sources2Index.defaultReturnValue(-1);
			sources = new ObjectArrayList<>();
		}

		public void add(ResourceLocation source) {
			if (sources2Index.putIfAbsent(source, index) == -1) {
				sources.add(source);
				index++;
			}
		}

		public Index build() {
			return new Index(this);
		}
	}
}
