package com.jozufozu.flywheel.api.visual;

import java.util.List;

import com.jozufozu.flywheel.api.instance.Instance;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityVisual<T extends BlockEntity> extends Visual {
	List<Instance> getCrumblingInstances();
}
