package com.jozufozu.flywheel.api.instance;

import java.util.List;

import com.jozufozu.flywheel.api.struct.InstancePart;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface BlockEntityInstance<T extends BlockEntity> extends Instance {
	List<InstancePart> getCrumblingParts();
}
