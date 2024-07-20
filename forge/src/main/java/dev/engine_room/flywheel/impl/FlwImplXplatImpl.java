package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.common.MinecraftForge;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchReloadLevelRendererEvent(ClientLevel level) {
		MinecraftForge.EVENT_BUS.post(new ReloadLevelRendererEvent(level));
	}

	@Override
	public String getVersionStr() {
		return FlywheelForge.version().toString();
	}

	@Override
	public FlwConfig getConfig() {
		return ForgeFlwConfig.INSTANCE;
	}
}
