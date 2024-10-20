package dev.engine_room.flywheel.api.backend;

public record BackendVersion(int major, int minor) implements Comparable<BackendVersion> {
	@Override
	public int compareTo(BackendVersion o) {
		if (major != o.major) {
			return Integer.compare(major, o.major);
		}
		return Integer.compare(minor, o.minor);
	}
}
