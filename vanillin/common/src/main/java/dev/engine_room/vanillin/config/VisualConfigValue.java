package dev.engine_room.vanillin.config;

import com.google.gson.annotations.SerializedName;

public enum VisualConfigValue {
	@SerializedName("default")
	DEFAULT,
	@SerializedName("disable")
	DISABLE,
	@SerializedName("force_enable")
	FORCE_ENABLE,
}
