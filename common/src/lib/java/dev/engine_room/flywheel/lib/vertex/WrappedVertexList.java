package dev.engine_room.flywheel.lib.vertex;

import dev.engine_room.flywheel.api.vertex.MutableVertexList;

public class WrappedVertexList implements MutableVertexList {
	protected final MutableVertexList delegate;

	public WrappedVertexList(MutableVertexList delegate) {
		this.delegate = delegate;
	}

	@Override
	public void x(int index, float x) {
		delegate.x(index, x);
	}

	@Override
	public void y(int index, float y) {
		delegate.y(index, y);
	}

	@Override
	public void z(int index, float z) {
		delegate.z(index, z);
	}

	@Override
	public void r(int index, float r) {
		delegate.r(index, r);
	}

	@Override
	public void g(int index, float g) {
		delegate.g(index, g);
	}

	@Override
	public void b(int index, float b) {
		delegate.b(index, b);
	}

	@Override
	public void a(int index, float a) {
		delegate.a(index, a);
	}

	@Override
	public void u(int index, float u) {
		delegate.u(index, u);
	}

	@Override
	public void v(int index, float v) {
		delegate.v(index, v);
	}

	@Override
	public void overlay(int index, int overlay) {
		delegate.overlay(index, overlay);
	}

	@Override
	public void light(int index, int light) {
		delegate.light(index, light);
	}

	@Override
	public void normalX(int index, float normalX) {
		delegate.normalX(index, normalX);
	}

	@Override
	public void normalY(int index, float normalY) {
		delegate.normalY(index, normalY);
	}

	@Override
	public void normalZ(int index, float normalZ) {
		delegate.normalZ(index, normalZ);
	}

	@Override
	public float x(int index) {
		return delegate.x(index);
	}

	@Override
	public float y(int index) {
		return delegate.y(index);
	}

	@Override
	public float z(int index) {
		return delegate.z(index);
	}

	@Override
	public float r(int index) {
		return delegate.r(index);
	}

	@Override
	public float g(int index) {
		return delegate.g(index);
	}

	@Override
	public float b(int index) {
		return delegate.b(index);
	}

	@Override
	public float a(int index) {
		return delegate.a(index);
	}

	@Override
	public float u(int index) {
		return delegate.u(index);
	}

	@Override
	public float v(int index) {
		return delegate.v(index);
	}

	@Override
	public int overlay(int index) {
		return delegate.overlay(index);
	}

	@Override
	public int light(int index) {
		return delegate.light(index);
	}

	@Override
	public float normalX(int index) {
		return delegate.normalX(index);
	}

	@Override
	public float normalY(int index) {
		return delegate.normalY(index);
	}

	@Override
	public float normalZ(int index) {
		return delegate.normalZ(index);
	}

	@Override
	public int vertexCount() {
		return delegate.vertexCount();
	}
}
