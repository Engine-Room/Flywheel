package dev.engine_room.flywheel.impl;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

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
		return FlywheelForge.version().toString();
	}

	@Override
	public FlwConfig getConfig() {
		return ForgeFlwConfig.INSTANCE;
	}

	@Override
	public Supplier<Boolean> getModLoaded(String modId) {
		return Suppliers.memoize(() -> LoadingModList.get().getModFileById(modId) != null);
	}
}
