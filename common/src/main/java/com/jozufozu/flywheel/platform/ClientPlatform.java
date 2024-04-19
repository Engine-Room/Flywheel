package com.jozufozu.flywheel.platform;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;

import net.minecraft.client.multiplayer.ClientLevel;

public abstract class ClientPlatform {
	private static final ClientPlatform INSTANCE;

	static {
		try {
			INSTANCE =
					(ClientPlatform) Class.forName("com.jozufozu.flywheel.platform.ClientPlatformImpl").getConstructor()
							.newInstance();
		} catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
				 NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static ClientPlatform getInstance() {
		return INSTANCE;
	}

	public abstract void dispatchReloadLevelRenderer(ClientLevel level);

	public abstract void dispatchBeginFrame(RenderContext context);

	public abstract void dispatchRenderStage(RenderContext context, RenderStage stage);

	public abstract boolean isModLoaded(String modid);

	@Nullable
	public abstract ShadersModHandler.InternalHandler createIrisOculusHandlerIfPresent();
}
