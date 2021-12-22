package com.jozufozu.flywheel.util;

import java.util.Objects;

public record Pair<F, S>(F first, S second) {

	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<>(first, second);
	}

	public Pair<S, F> swap() {
		return Pair.of(second, first);
	}

	public Pair<F, S> copy() {
		return Pair.of(first, second);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) return true;
		if (obj instanceof final Pair<?, ?> other) {
			return Objects.equals(first, other.first) && Objects.equals(second, other.second);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (nullHash(first) * 31) ^ nullHash(second);
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	static int nullHash(Object o) {
		return o == null ? 0 : o.hashCode();
	}

}
