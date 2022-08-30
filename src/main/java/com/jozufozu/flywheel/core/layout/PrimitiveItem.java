package com.jozufozu.flywheel.core.layout;

import java.util.function.Consumer;
import java.util.function.Function;

import com.jozufozu.flywheel.backend.gl.array.VertexAttribute;
import com.jozufozu.flywheel.core.source.generate.GlslExpr;

public class PrimitiveItem implements InputType {

	private final VertexAttribute attribute;
	private final String typeName;
	private final String packedTypeName;
	private final Function<GlslExpr, GlslExpr> unpackingFunction;

	public PrimitiveItem(VertexAttribute attribute, String typeName, String packedTypeName, Function<GlslExpr, GlslExpr> unpackingFunction) {
		this.attribute = attribute;
		this.typeName = typeName;
		this.packedTypeName = packedTypeName;
		this.unpackingFunction = unpackingFunction;
	}

	@Override
	public void provideAttributes(Consumer<VertexAttribute> consumer) {
		consumer.accept(attribute);
	}

	@Override
	public String typeName() {
		return typeName;
	}

	@Override
	public String packedTypeName() {
		return packedTypeName;
	}

	@Override
	public int attributeCount() {
		return 1;
	}

	public static PrimitiveItemBuilder builder() {
		return new PrimitiveItemBuilder();
	}

	@Override
	public GlslExpr unpack(GlslExpr packed) {
		return unpackingFunction.apply(packed);
	}

	public static class PrimitiveItemBuilder {
		private VertexAttribute attribute;
		private String typeName;
		private String packedTypeName;
		private Function<GlslExpr, GlslExpr> unpackingFunction = Function.identity();

		public PrimitiveItemBuilder setAttribute(VertexAttribute attribute) {
			this.attribute = attribute;
			return this;
		}

		public PrimitiveItemBuilder setTypeName(String typeName) {
			this.typeName = typeName;
			return this;
		}

		public PrimitiveItemBuilder setPackedTypeName(String packedTypeName) {
			this.packedTypeName = packedTypeName;
			return this;
		}

		public PrimitiveItemBuilder unpack(Function<GlslExpr, GlslExpr> f) {
			this.unpackingFunction = f;
			return this;
		}

		public PrimitiveItem createPrimitiveItem() {
			return new PrimitiveItem(attribute, typeName, packedTypeName, unpackingFunction);
		}
	}
}
