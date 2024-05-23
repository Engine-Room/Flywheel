package dev.engine_room.flywheel.lib.backend;

import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendManager;
import dev.engine_room.flywheel.api.backend.Engine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public class SimpleBackend implements Backend {
	private final Function<LevelAccessor, Engine> engineFactory;
	private final Supplier<Backend> fallback;
	private final BooleanSupplier isSupported;

	public SimpleBackend(Function<LevelAccessor, Engine> engineFactory, Supplier<Backend> fallback, BooleanSupplier isSupported) {
		this.engineFactory = engineFactory;
		this.fallback = fallback;
		this.isSupported = isSupported;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Engine createEngine(LevelAccessor level) {
		return engineFactory.apply(level);
	}

	@Override
	public Backend findFallback() {
		if (isSupported()) {
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
		private Function<LevelAccessor, Engine> engineFactory;
		private Supplier<Backend> fallback = BackendManager::getOffBackend;
		private BooleanSupplier isSupported;

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
			return Backend.REGISTRY.registerAndGet(id, new SimpleBackend(engineFactory, fallback, isSupported));
		}
	}
}
