package dev.engine_room.flywheel.backend.glsl.generate;

public class GlslVertexInput implements GlslBuilder.Declaration {
	private int binding;
	private String type;
	private String name;

	public GlslVertexInput binding(int binding) {
		this.binding = binding;
		return this;
	}

	public GlslVertexInput type(String type) {
		this.type = type;
		return this;
	}

	public GlslVertexInput name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public String prettyPrint() {
		return "layout(location = " + binding + ") in " + type + " " + name + ";";
	}
}
