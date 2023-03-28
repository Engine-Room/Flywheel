package com.jozufozu.flywheel.backend;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.core.BackendTypes;
import com.jozufozu.flywheel.core.pipeline.SimplePipeline;

import net.minecraft.network.chat.Component;

public class SimpleBackendType implements BackendType {


	private final String properName;
	private final String shortName;
	private final Component engineMessage;
	private final Supplier<Engine> engineSupplier;
	private final Supplier<BackendType> fallback;
	private final BooleanSupplier isSupported;
	private final SimplePipeline pipelineShader;

	public SimpleBackendType(String properName, String shortName, Component engineMessage, Supplier<Engine> engineSupplier, Supplier<BackendType> fallback, BooleanSupplier isSupported, @Nullable SimplePipeline pipelineShader) {
		this.properName = properName;
		this.shortName = shortName;
		this.engineMessage = engineMessage;
		this.engineSupplier = engineSupplier;
		this.fallback = fallback;
		this.isSupported = isSupported;
		this.pipelineShader = pipelineShader;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getProperName() {
		return properName;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public Component getEngineMessage() {
		return engineMessage;
	}

	@Override
	public Engine createEngine() {
		return engineSupplier.get();
	}

	@Override
	public BackendType findFallback() {
		if (this.supported()) {
			return this;
		} else {
			return fallback.get()
					.findFallback();
		}
	}

	@Override
	public boolean supported() {
		return isSupported.getAsBoolean();
	}

	@Override
	public @Nullable SimplePipeline pipelineShader() {
		return pipelineShader;
	}

	public static class Builder {
		private String properName;
		private String shortName;
		private Component engineMessage;
		private Supplier<Engine> engineSupplier;
		private Supplier<BackendType> fallback;
		private BooleanSupplier isSupported;
		private SimplePipeline pipelineShader;

		public Builder properName(String properName) {
			this.properName = properName;
			return this;
		}

		public Builder shortName(String shortName) {
			this.shortName = shortName;
			return this;
		}

		public Builder engineMessage(Component engineMessage) {
			this.engineMessage = engineMessage;
			return this;
		}

		public Builder engineSupplier(Supplier<Engine> engineSupplier) {
			this.engineSupplier = engineSupplier;
			return this;
		}

		public Builder fallback(Supplier<BackendType> fallback) {
			this.fallback = fallback;
			return this;
		}

		public Builder supported(BooleanSupplier isSupported) {
			this.isSupported = isSupported;
			return this;
		}

		public Builder pipelineShader(SimplePipeline pipelineShader) {
			this.pipelineShader = pipelineShader;
			return this;
		}

		public BackendType register() {
			return BackendTypes.register(new SimpleBackendType(properName, shortName, engineMessage, engineSupplier, fallback, isSupported, pipelineShader));
		}
	}
}
