package dev.engine_room.flywheel.backend.glsl.error.lines;

public record FileLine(String fileName) implements ErrorLine {
	@Override
	public String left() {
		return "-";
	}

	@Override
	public Divider divider() {
		return Divider.ARROW;
	}

	@Override
	public String right() {
		return fileName;
	}
}
