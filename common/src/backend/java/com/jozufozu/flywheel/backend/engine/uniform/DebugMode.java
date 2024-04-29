package com.jozufozu.flywheel.backend.engine.uniform;

import java.util.Locale;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

public enum DebugMode implements StringRepresentable {
	OFF,
	NORMALS,
	INSTANCE_ID,
	LIGHT_LEVEL,
	LIGHT_COLOR,
	OVERLAY,
	DIFFUSE,
	LIGHT_VOLUME,
	;

	public static final Codec<DebugMode> CODEC = StringRepresentable.fromEnum(DebugMode::values);

	@Override
	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}
}
