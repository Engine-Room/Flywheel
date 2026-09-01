package dev.engine_room.vanillin.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ModOverrides(Map<String, List<VisualOverride>> blockEntities, Map<String, List<VisualOverride>> entities) {
	public ModOverrides(List<VisualOverride> blockEntities, List<VisualOverride> entities) {
		this(sort(blockEntities), sort(entities));
	}

	public static Map<String, List<VisualOverride>> sort(List<VisualOverride> list) {
		return list.stream().collect(Collectors.groupingBy(VisualOverride::name));
	}
}
