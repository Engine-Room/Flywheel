package com.jozufozu.flywheel.glsl.generate;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;

public interface GlslExpr {

	/**
	 * Create a glsl variable with the given name.
	 *
	 * @param name The name of the variable.
	 * @return A new variable expression.
	 */
	static Variable variable(String name) {
		return new Variable(name);
	}

	static FunctionCall call(String functionName, Collection<? extends GlslExpr> args) {
		return new FunctionCall(functionName, args);
	}

	static FunctionCall0 call(String functionName) {
		return new FunctionCall0(functionName);
	}

	static GlslExpr literal(int expr) {
		return new IntLiteral(expr);
	}

	static GlslExpr literal(boolean expr) {
		return new BoolLiteral(expr);
	}

	/**
	 * Call a one-parameter function with the given name on this expression.
	 *
	 * @param name The name of the function to call.
	 * @return A new glsl function call expression.
	 */
	default FunctionCall callFunction(String name) {
		return new FunctionCall(name, this);
	}

	/**
	 * Swizzle the components of this expression.
	 *
	 * @param selection The components to select. For example, "xyz", "zyx", or "zzzw".
	 * @return A new glsl swizzle expression.
	 */
	default Swizzle swizzle(String selection) {
		return new Swizzle(this, selection);
	}

	/**
	 * Access the given member of this expression.
	 *
	 * @param member The name of the member to access.
	 * @return A new glsl member access expression.
	 */
	default Access access(String member) {
		return new Access(this, member);
	}

	/**
	 * Catchall method for applying external transformations to this expression.
	 *
	 * @param f The transformation to apply.
	 * @return A new expression.
	 */
	default GlslExpr transform(Function<GlslExpr, GlslExpr> f) {
		return f.apply(this);
	}

	String prettyPrint();

	record Variable(String name) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return name;
		}

	}

	record FunctionCall(String name, Collection<? extends GlslExpr> args) implements GlslExpr {
		public FunctionCall(String name, GlslExpr target) {
			this(name, ImmutableList.of(target));
		}

		@Override
		public String prettyPrint() {
			var args = this.args.stream()
					.map(GlslExpr::prettyPrint)
					.collect(Collectors.joining(", "));
			return name + "(" + args + ")";
		}

	}

	record FunctionCall0(String name) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return name + "()";
		}

	}

	record Swizzle(GlslExpr target, String selection) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return target.prettyPrint() + "." + selection;
		}

	}

	record Access(GlslExpr target, String argName) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return target.prettyPrint() + "." + argName;
		}

	}

	record IntLiteral(int value) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return Integer.toString(value);
		}
	}

	record BoolLiteral(boolean value) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return Boolean.toString(value);
		}
	}
}
