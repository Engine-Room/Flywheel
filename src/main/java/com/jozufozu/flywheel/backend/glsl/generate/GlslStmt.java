package com.jozufozu.flywheel.backend.glsl.generate;

public interface GlslStmt {
	GlslStmt BREAK = () -> "break;";
	GlslStmt CONTINUE = () -> "continue;";
	GlslStmt RETURN = () -> "return;";

	static GlslStmt eval(GlslExpr expr) {
		return new Eval(expr);
	}

	static GlslStmt ret(GlslExpr value) {
		return new Return(value);
	}

	static GlslStmt raw(String s) {
		return new Raw(s);
	}

	String prettyPrint();

	record Eval(GlslExpr expr) implements GlslStmt {
		@Override
		public String prettyPrint() {
			return expr.prettyPrint() + ";";
		}
	}

	record Return(GlslExpr expr) implements GlslStmt {
		@Override
		public String prettyPrint() {
			return "return " + expr.prettyPrint() + ";";
		}
	}

	record Raw(String glsl) implements GlslStmt {
		@Override
		public String prettyPrint() {
			return glsl;
		}
	}
}
