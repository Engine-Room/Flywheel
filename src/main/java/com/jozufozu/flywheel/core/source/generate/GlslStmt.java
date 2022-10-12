package com.jozufozu.flywheel.core.source.generate;

import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.util.Pair;

public interface GlslStmt extends LangItem {

	static GlslStmt eval(GlslExpr expr) {
		return new Eval(expr);
	}

	static GlslStmt ret(GlslExpr value) {
		return new Return(value);
	}

	static GlslStmt BREAK = () -> "break;";

	static GlslStmt CONTINUE = () -> "continue;";

	static GlslStmt RETURN = () -> "return;";

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

	record Switch(GlslExpr expr, List<Pair<GlslExpr, GlslBuilder.BlockBuilder>> body) implements GlslStmt {
		@Override
		public String prettyPrint() {
			var cases = body.stream()
					.map(Switch::prettyPrintCase)
					.collect(Collectors.joining("\n"));
			return """
					switch (%s) {
					%s
					}""".formatted(expr.prettyPrint(), cases);
		}

		private static String prettyPrintCase(Pair<GlslExpr, GlslBuilder.BlockBuilder> p) {
			var variant = p.first()
					.prettyPrint();
			var block = p.second()
					.prettyPrint();
			return """
					case %s:
					%s""".formatted(variant, block.indent(4));
		}
	}
}
