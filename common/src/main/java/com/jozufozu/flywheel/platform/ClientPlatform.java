package com.jozufozu.flywheel.platform;

import java.lang.reflect.InvocationTargetException;

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
}
