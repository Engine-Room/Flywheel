package com.jozufozu.flywheel.backend;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;

public class IrisShaderHandler {
	public static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");

	private static final InternalHandler HANDLER;

	static {
		if (IRIS_LOADED) {
			HANDLER = new InternalHandlerImpl();
		} else {
			HANDLER = new InternalHandler() {};
		}
	}

	private IrisShaderHandler() {
	}

	public static boolean isShaderPackInUse() {
		return HANDLER.isShaderPackInUse();
	}

	public static boolean isRenderingShadowPass() {
		return HANDLER.isRenderingShadowPass();
	}

	private interface InternalHandler {
		default boolean isShaderPackInUse() {
			return false;
		};

		default boolean isRenderingShadowPass() {
			return false;
		};
	}

	private static class InternalHandlerImpl implements InternalHandler {
		@Override
		public boolean isShaderPackInUse() {
			return IrisApi.getInstance().isShaderPackInUse();
		};

		@Override
		public boolean isRenderingShadowPass() {
			return IrisApi.getInstance().isRenderingShadowPass();
		};
	}
}
