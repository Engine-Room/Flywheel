package dev.engine_room.flywheel.backend.glsl.generate;

public class GlslUniform implements GlslBuilder.Declaration {
	private String type;
	private String name;

	public GlslUniform type(String typeName) {
		type = typeName;
		return this;
	}

	public GlslUniform name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String prettyPrint() {
		return "uniform " + type + " " + name + ";";
	}
}
