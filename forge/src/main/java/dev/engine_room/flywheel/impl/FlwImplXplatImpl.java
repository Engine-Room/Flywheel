package dev.engine_room.flywheel.impl;

import java.util.function.BooleanSupplier;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.NeoForge;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchReloadLevelRendererEvent(ClientLevel level) {
		NeoForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public String getVersionStr() {
		return FlywheelNeoForge.version().toString();
	}

	@Override
	public FlwConfig getConfig() {
		return NeoForgeFlwConfig.INSTANCE;
	}

	@Override
	public BooleanSupplier getModLoaded(String modId) {
		return () -> LoadingModList.get().getModFileById(modId) != null;
	}
}
