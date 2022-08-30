package com.jozufozu.flywheel.core.source.generate;

import java.util.function.Function;

public sealed interface GlslExpr {

	/**
	 * Create a glsl variable with the given name.
	 *
	 * @param name The name of the variable.
	 * @return A new variable expression.
	 */
	static Variable variable(String name) {
		return new Variable(name);
	}

	String minPrint();

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

	record Variable(String name) implements GlslExpr {
		@Override
		public String minPrint() {
			return name;
		}
	}

	record FunctionCall(String name, GlslExpr target) implements GlslExpr {
		@Override
		public String minPrint() {
			return name + "(" + target.minPrint() + ")";
		}
	}

	record Swizzle(GlslExpr target, String selection) implements GlslExpr {
		@Override
		public String minPrint() {
			return target.minPrint() + "." + selection;
		}
	}

	record Access(GlslExpr target, String argName) implements GlslExpr {
		@Override
		public String minPrint() {
			return target.minPrint() + "." + argName;
		}
	}
}
