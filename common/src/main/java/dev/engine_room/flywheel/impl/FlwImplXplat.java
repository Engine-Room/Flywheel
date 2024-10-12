package dev.engine_room.flywheel.impl;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.api.internal.DependencyInjection;
import net.minecraft.client.multiplayer.ClientLevel;

public interface FlwImplXplat {
	FlwImplXplat INSTANCE = DependencyInjection.load(FlwImplXplat.class, "dev.engine_room.flywheel.impl.FlwImplXplatImpl");

	void dispatchReloadLevelRendererEvent(ClientLevel level);

	String getVersionStr();

	FlwConfig getConfig();

	BooleanSupplier getModLoaded(String modId);
}
