package dev.engine_room.flywheel.lib.instance;

import java.util.Objects;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.InstanceWriter;
import dev.engine_room.flywheel.api.layout.Layout;
import net.minecraft.resources.Identifier;

public final class SimpleInstanceType<I extends Instance> implements InstanceType<I> {
	private final Factory<I> factory;
	private final Layout layout;
	private final InstanceWriter<I> writer;
	private final Identifier vertexShader;
	private final Identifier cullShader;

	public SimpleInstanceType(Factory<I> factory, Layout layout, InstanceWriter<I> writer, Identifier vertexShader, Identifier cullShader) {
		this.factory = factory;
		this.layout = layout;
		this.writer = writer;
		this.vertexShader = vertexShader;
		this.cullShader = cullShader;
	}

	public static <I extends Instance> Builder<I> builder(Factory<I> factory) {
		return new Builder<>(factory);
	}

	@Override
	public I create(InstanceHandle handle) {
		return factory.create(this, handle);
	}

	@Override
	public Layout layout() {
		return layout;
	}

	@Override
	public InstanceWriter<I> writer() {
		return writer;
	}

	@Override
	public Identifier vertexShader() {
		return vertexShader;
	}

	@Override
	public Identifier cullShader() {
		return cullShader;
	}

	@FunctionalInterface
	public interface Factory<I extends Instance> {
		I create(InstanceType<I> type, InstanceHandle handle);
	}

	public static final class Builder<I extends Instance> {
		private final Factory<I> factory;
		private Layout layout;
		private InstanceWriter<I> writer;
		private Identifier vertexShader;
		private Identifier cullShader;

		public Builder(Factory<I> factory) {
			this.factory = factory;
		}

		public Builder<I> layout(Layout layout) {
			this.layout = layout;
			return this;
		}

		public Builder<I> writer(InstanceWriter<I> writer) {
			this.writer = writer;
			return this;
		}

		public Builder<I> vertexShader(Identifier vertexShader) {
			this.vertexShader = vertexShader;
			return this;
		}

		public Builder<I> cullShader(Identifier cullShader) {
			this.cullShader = cullShader;
			return this;
		}

		public SimpleInstanceType<I> build() {
			Objects.requireNonNull(layout);
			Objects.requireNonNull(writer);
			Objects.requireNonNull(vertexShader);
			Objects.requireNonNull(cullShader);

			return new SimpleInstanceType<>(factory, layout, writer, vertexShader, cullShader);
		}
	}
}
