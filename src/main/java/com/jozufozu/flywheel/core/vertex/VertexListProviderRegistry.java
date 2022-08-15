package com.jozufozu.flywheel.core.vertex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.mojang.blaze3d.vertex.VertexFormat;

public class VertexListProviderRegistry {
	private static final Map<VertexFormat, Holder> HOLDERS = new ConcurrentHashMap<>();

	private static Holder getOrCreateHolder(VertexFormat format) {
		return HOLDERS.computeIfAbsent(format, Holder::new);
	}

	public static void register(VertexFormat format, VertexListProvider provider) {
		getOrCreateHolder(format).registeredProvider = provider;
	}

	@Nullable
	public static VertexListProvider get(VertexFormat format) {
		return getOrCreateHolder(format).get();
	}

	public static VertexListProvider getOrInfer(VertexFormat format) {
		return getOrCreateHolder(format).getOrInfer();
	}

	private static class Holder {
		public final VertexFormat format;
		public VertexListProvider registeredProvider;
		public VertexListProvider inferredProvider;

		public Holder(VertexFormat format) {
			this.format = format;
		}

		@Nullable
		public VertexListProvider get() {
			return registeredProvider;
		}

		public VertexListProvider getOrInfer() {
			if (registeredProvider != null) {
				return registeredProvider;
			}
			if (inferredProvider == null) {
				inferredProvider = new InferredVertexListProviderImpl(format);
			}
			return inferredProvider;
		}
	}
}
