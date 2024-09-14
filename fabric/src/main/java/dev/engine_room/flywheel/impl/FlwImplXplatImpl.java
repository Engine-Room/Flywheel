package dev.engine_room.flywheel.impl;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;

public class FlwImplXplatImpl implements FlwImplXplat {
	@Override
	public void dispatchReloadLevelRendererEvent(ClientLevel level) {
		ReloadLevelRendererCallback.EVENT.invoker().onReloadLevelRenderer(level);
	}

	@Override
	public String getVersionStr() {
		return FlywheelFabric.version().getFriendlyString();
	}

	@Override
	public FlwConfig getConfig() {
		return FabricFlwConfig.INSTANCE;
	}

	@Override
	public Supplier<Boolean> getModLoaded(String modId) {
		return Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded(modId));
	}
}
