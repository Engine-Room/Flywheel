package com.jozufozu.flywheel.api.instance;

import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancedPart;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityInstance<T extends BlockEntity> extends Instance {
	List<InstancedPart> getCrumblingParts();
}
