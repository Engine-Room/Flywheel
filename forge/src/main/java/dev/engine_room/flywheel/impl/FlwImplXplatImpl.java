package dev.engine_room.flywheel.impl;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.impl.compat.CompatMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.LoadingModList;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}

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

	@Override
	public boolean useSodium0_6Compat() {
		return CompatMod.SODIUM.isLoaded && !CompatMod.EMBEDDIUM.isLoaded;
	}

	@Override
	public boolean useIrisCompat() {
		return CompatMod.IRIS.isLoaded || CompatMod.OCULUS.isLoaded;
	}
}
