package com.jozufozu.flywheel.glsl.generate;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.lib.util.Pair;

public record FnSignature(String returnType, String name, ImmutableList<Pair<String, String>> args) {

	public static Builder create() {
		return new Builder();
	}

	public static FnSignature of(String returnType, String name) {
		return FnSignature.create()
				.returnType(returnType)
				.name(name)
				.build();
	}

	public static FnSignature ofVoid(String name) {
		return new FnSignature("void", name, ImmutableList.of());
	}

	public Collection<? extends GlslExpr> createArgExpressions() {
		return args.stream()
				.map(Pair::second)
				.map(GlslExpr::variable)
				.collect(Collectors.toList());
	}

	public boolean isVoid() {
		return "void".equals(returnType);
	}

	public String fullDeclaration() {
		return returnType + ' ' + name + '(' + args.stream()
				.map(p -> p.first() + ' ' + p.second())
				.collect(Collectors.joining(", ")) + ')';
	}

	public String signatureDeclaration() {
		return returnType + ' ' + name + '(' + args.stream()
				.map(Pair::first)
				.collect(Collectors.joining(", ")) + ')';
	}

	public static class Builder {
		private String returnType;
		private String name;
		private final ImmutableList.Builder<Pair<String, String>> args = ImmutableList.builder();

		public Builder returnType(String returnType) {
			this.returnType = returnType;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder arg(String type, String name) {
			args.add(Pair.of(type, name));
			return this;
		}

		public FnSignature build() {
			if (returnType == null) {
				throw new IllegalStateException("returnType not set");
			}
			if (name == null) {
				throw new IllegalStateException("name not set");
			}
			return new FnSignature(returnType, name, args.build());
		}
	}

}
