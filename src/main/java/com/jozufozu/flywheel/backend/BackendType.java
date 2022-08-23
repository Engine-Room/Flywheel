package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.backend.instancing.Engine;

import net.minecraft.network.chat.Component;

public interface BackendType {

	String getProperName();

	String getShortName();

	Component getEngineMessage();

	Engine createEngine();

	BackendType findFallback();

	boolean supported();
}
