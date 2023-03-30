package com.jozufozu.flywheel.lib.layout;

import java.util.function.Consumer;
import java.util.function.Function;

import com.jozufozu.flywheel.api.layout.InputType;
import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.glsl.generate.GlslExpr;

public class VecInput implements InputType {

	private final VertexAttribute attribute;
	private final String typeName;
	private final String packedTypeName;
	private final Function<GlslExpr, GlslExpr> unpackingFunction;

	public VecInput(VertexAttribute attribute, String typeName, String packedTypeName, Function<GlslExpr, GlslExpr> unpackingFunction) {
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

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public GlslExpr unpack(GlslExpr packed) {
		return unpackingFunction.apply(packed);
	}

	public static class Builder {
		private VertexAttribute attribute;
		private String typeName;
		private String packedTypeName;
		private Function<GlslExpr, GlslExpr> unpackingFunction = Function.identity();

		public Builder vertexAttribute(VertexAttribute attribute) {
			this.attribute = attribute;
			return this;
		}

		public Builder typeName(String typeName) {
			this.typeName = typeName;
			return this;
		}

		public Builder packedTypeName(String packedTypeName) {
			this.packedTypeName = packedTypeName;
			return this;
		}

		public Builder unpackingFunction(Function<GlslExpr, GlslExpr> f) {
			this.unpackingFunction = f;
			return this;
		}

		public VecInput build() {
			return new VecInput(attribute, typeName, packedTypeName, unpackingFunction);
		}
	}
}
