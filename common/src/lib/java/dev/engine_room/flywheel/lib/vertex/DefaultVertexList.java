package dev.engine_room.flywheel.lib.vertex;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public interface DefaultVertexList extends MutableVertexList {
	@Override
	default float x(int index) {
		return 0.0f;
	}

	@Override
	default float y(int index) {
		return 0.0f;
	}

	@Override
	default float z(int index) {
		return 0.0f;
	}

	@Override
	default float r(int index) {
		return 1.0f;
	}

	@Override
	default float g(int index) {
		return 1.0f;
	}

	@Override
	default float b(int index) {
		return 1.0f;
	}

	@Override
	default float a(int index) {
		return 1.0f;
	}

	@Override
	default float u(int index) {
		return 0.0f;
	}

	@Override
	default float v(int index) {
		return 0.0f;
	}

	@Override
	default int overlay(int index) {
		return OverlayTexture.NO_OVERLAY;
	}

	@Override
	default int light(int index) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	default float normalX(int index) {
		return 0.0f;
	}

	@Override
	default float normalY(int index) {
		return 1.0f;
	}

	@Override
	default float normalZ(int index) {
		return 0.0f;
	}

	@Override
	default void x(int index, float x) {
	}

	@Override
	default void y(int index, float y) {
	}

	@Override
	default void z(int index, float z) {
	}

	@Override
	default void r(int index, float r) {
	}

	@Override
	default void g(int index, float g) {
	}

	@Override
	default void b(int index, float b) {
	}

	@Override
	default void a(int index, float a) {
	}

	@Override
	default void u(int index, float u) {
	}

	@Override
	default void v(int index, float v) {
	}

	@Override
	default void overlay(int index, int overlay) {
	}

	@Override
	default void light(int index, int light) {
	}

	@Override
	default void normalX(int index, float normalX) {
	}

	@Override
	default void normalY(int index, float normalY) {
	}

	@Override
	default void normalZ(int index, float normalZ) {
	}
}
