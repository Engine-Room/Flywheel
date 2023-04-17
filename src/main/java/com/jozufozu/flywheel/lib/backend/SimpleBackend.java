package com.jozufozu.flywheel.lib.backend;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.BackendManager;
import com.jozufozu.flywheel.api.backend.Engine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public class SimpleBackend implements Backend {
	private final Component engineMessage;
	private final Function<LevelAccessor, Engine> engineFactory;
	private final Supplier<Backend> fallback;
	private final BooleanSupplier isSupported;

	public SimpleBackend(Component engineMessage, Function<LevelAccessor, Engine> engineFactory, Supplier<Backend> fallback, BooleanSupplier isSupported) {
		this.engineMessage = engineMessage;
		this.engineFactory = engineFactory;
		this.fallback = fallback;
		this.isSupported = isSupported;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Component engineMessage() {
		return engineMessage;
	}

	@Override
	public Engine createEngine(LevelAccessor level) {
		return engineFactory.apply(level);
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

	public static class Builder {
		private Component engineMessage;
		private Function<LevelAccessor, Engine> engineFactory;
		private Supplier<Backend> fallback = BackendManager::getOffBackend;
		private BooleanSupplier isSupported;

		public Builder engineMessage(Component engineMessage) {
			this.engineMessage = engineMessage;
			return this;
		}

		public Builder engineFactory(Function<LevelAccessor, Engine> engineFactory) {
			this.engineFactory = engineFactory;
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

		public Backend register(ResourceLocation id) {
			return Backend.REGISTRY.registerAndGet(id, new SimpleBackend(engineMessage, engineFactory, fallback, isSupported));
		}
	}
}
