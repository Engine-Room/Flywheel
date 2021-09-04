package com.jozufozu.flywheel.light;

public interface IMovingListener extends ILightUpdateListener {
	boolean update(LightProvider provider);
}
