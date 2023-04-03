package com.jozufozu.flywheel.lib.backend;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.pipeline.Pipeline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SimpleBackend implements Backend {
	private final String properName;
	private final Component engineMessage;
	private final Supplier<Engine> engineSupplier;
	private final Supplier<Backend> fallback;
	private final BooleanSupplier isSupported;
	private final Pipeline pipelineShader;

	public SimpleBackend(String properName, Component engineMessage, Supplier<Engine> engineSupplier, Supplier<Backend> fallback, BooleanSupplier isSupported, @Nullable Pipeline pipelineShader) {
		this.properName = properName;
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
	public Component getEngineMessage() {
		return engineMessage;
	}

	@Override
	public Engine createEngine() {
		return engineSupplier.get();
	}

	@Override
	public Backend findFallback() {
		if (this.isSupported()) {
			return this;
		} else {
			return fallback.get()
					.findFallback();
		}
	}

	@Override
	public boolean isSupported() {
		return isSupported.getAsBoolean();
	}

	@Override
	public @Nullable Pipeline pipelineShader() {
		return pipelineShader;
	}

	public static class Builder {
		private String properName;
		private Component engineMessage;
		private Supplier<Engine> engineSupplier;
		private Supplier<Backend> fallback;
		private BooleanSupplier isSupported;
		private Pipeline pipelineShader;

		public Builder properName(String properName) {
			this.properName = properName;
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

		public Builder fallback(Supplier<Backend> fallback) {
			this.fallback = fallback;
			return this;
		}

		public Builder supported(BooleanSupplier isSupported) {
			this.isSupported = isSupported;
			return this;
		}

		public Builder pipelineShader(Pipeline pipelineShader) {
			this.pipelineShader = pipelineShader;
			return this;
		}

		public Backend register(ResourceLocation id) {
			return Backend.REGISTRY.registerAndGet(id, new SimpleBackend(properName, engineMessage, engineSupplier, fallback, isSupported, pipelineShader));
		}
	}
}
