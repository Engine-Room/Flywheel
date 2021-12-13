package com.jozufozu.flywheel.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class RecordingVertexConsumer implements VertexConsumer {

	List<Consumer<VertexConsumer>> replay = new ArrayList<>();

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		replay.add(v -> v.vertex(x, y, z));
		return this;
	}

	@Override
	public VertexConsumer color(int r, int g, int b, int a) {
		replay.add(v -> v.color(r, g, b, a));
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		replay.add(vc -> vc.uv(u, v));
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		replay.add(vc -> vc.overlayCoords(u, v));
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		replay.add(vc -> vc.uv2(u, v));
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		replay.add(v -> v.normal(x, y, z));
		return this;
	}

	@Override
	public void endVertex() {
		replay.add(VertexConsumer::endVertex);
	}

	@Override
	public void defaultColor(int r, int g, int b, int a) {
		replay.add(vc -> vc.defaultColor(r, g, b, a));
	}

	@Override
	public void unsetDefaultColor() {
		replay.add(VertexConsumer::unsetDefaultColor);
	}

	public VertexRecording saveRecording() {
		VertexRecording out = new VertexRecording(ImmutableList.copyOf(replay));
		replay.clear();
		return out;
	}
}
