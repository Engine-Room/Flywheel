package dev.engine_room.flywheel.impl;

import java.util.List;

import dev.engine_room.flywheel.api.backend.BackendVersion;

public record ModRequirements(BackendVersion minimumBackendVersion, List<Entry> entries) {
	public record Entry(String modId, BackendVersion version) {
	}
}
