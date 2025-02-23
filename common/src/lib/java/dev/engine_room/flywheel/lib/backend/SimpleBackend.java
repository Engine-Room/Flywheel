package dev.engine_room.flywheel.lib.backend;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.Engine;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public final class SimpleBackend implements Backend {
	private final Function<LevelAccessor, Engine> engineFactory;
	private final PriorityProvider priority;
	private final BooleanSupplier isSupported;

	public SimpleBackend(PriorityProvider priority, Function<LevelAccessor, Engine> engineFactory, BooleanSupplier isSupported) {
		this.priority = priority;
		this.engineFactory = engineFactory;
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
	public int priority() {
		return priority.get();
	}

	@Override
	public boolean isSupported() {
		return isSupported.getAsBoolean();
	}

	public interface PriorityProvider {
		int get();
	}

	public static final class Builder {
		private Function<LevelAccessor, Engine> engineFactory;
		private PriorityProvider priority = () -> 0;
		private BooleanSupplier isSupported;

		public Builder engineFactory(Function<LevelAccessor, Engine> engineFactory) {
			this.engineFactory = engineFactory;
			return this;
		}

		public Builder priority(int priority) {
			return priority(() -> priority);
		}

		public Builder priority(PriorityProvider priority) {
			this.priority = priority;
			return this;
		}

		public Builder supported(BooleanSupplier isSupported) {
			this.isSupported = isSupported;
			return this;
		}

		public Backend register(ResourceLocation id) {
			Objects.requireNonNull(engineFactory);
			Objects.requireNonNull(isSupported);

			return Backend.REGISTRY.registerAndGet(id, new SimpleBackend(priority, engineFactory, isSupported));
		}
	}
}
