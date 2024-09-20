package dev.engine_room.flywheel.backend.glsl.error.lines;

public class SpanHighlightLine implements ErrorLine {
	private final String line;

	public SpanHighlightLine(int firstCol, int lastCol) {
		line = generateUnderline(firstCol, lastCol);
	}

	@Override
	public String right() {
		return line;
	}

	public static String generateUnderline(int firstCol, int lastCol) {
		return " ".repeat(Math.max(0, firstCol)) + "^".repeat(Math.max(0, lastCol - firstCol));
	}
}
