package dev.engine_room.flywheel.backend.glsl.generate;

import java.util.Collection;
import java.util.List;
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

	static FunctionCall call(String functionName, GlslExpr... args) {
		return new FunctionCall(functionName, List.of(args));
	}

	static FunctionCall call(String functionName, Collection<? extends GlslExpr> args) {
		return new FunctionCall(functionName, args);
	}

	static FunctionCall0 call(String functionName) {
		return new FunctionCall0(functionName);
	}

	static GlslExpr intLiteral(int expr) {
		return new RawLiteral(Integer.toString(expr));
	}

	static GlslExpr uintLiteral(int expr) {
		return new RawLiteral(Integer.toUnsignedString(expr) + 'u');
	}

	static GlslExpr uintHexLiteral(int expr) {
		return new RawLiteral("0x" + Integer.toHexString(expr) + 'u');
	}

	static GlslExpr boolLiteral(boolean expr) {
		return new RawLiteral(Boolean.toString(expr));
	}

	static GlslExpr floatLiteral(float expr) {
		return new RawLiteral(Float.toString(expr));
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

	default FunctionCall cast(String name) {
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

	default GlslExpr div(float v) {
		return new Binary(this, floatLiteral(v), BinOp.DIVIDE);
	}

	default GlslExpr sub(int v) {
		return new Binary(this, uintLiteral(v), BinOp.SUBTRACT);
	}

	default GlslExpr rsh(int by) {
		if (by == 0) {
			return this;
		}
		return new Binary(this, uintLiteral(by), BinOp.RIGHT_SHIFT);
	}

	default GlslExpr and(int mask) {
		return new Binary(this, uintHexLiteral(mask), BinOp.BITWISE_AND);
	}

	default GlslExpr xor(int mask) {
		return new Binary(this, uintHexLiteral(mask), BinOp.BITWISE_XOR);
	}

	default GlslExpr clamp(float from, float to) {
		return new Clamp(this, floatLiteral(from), floatLiteral(to));
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

	record Clamp(GlslExpr value, GlslExpr from, GlslExpr to) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return "clamp(" + value.prettyPrint() + ", " + from.prettyPrint() + ", " + to.prettyPrint() + ")";
		}
	}

	record Binary(GlslExpr lhs, GlslExpr rhs, BinOp op) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return "(" + lhs.prettyPrint() + " " + op.op + " " + rhs.prettyPrint() + ")";
		}
	}

	record RawLiteral(String value) implements GlslExpr {
		@Override
		public String prettyPrint() {
			return value;
		}
	}
}
