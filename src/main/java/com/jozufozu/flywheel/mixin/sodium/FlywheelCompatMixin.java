package com.jozufozu.flywheel.mixin.sodium;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import me.jellysquid.mods.sodium.client.compat.FlywheelCompat;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 
 * Overwrite all methods in this class with stubs. Flywheel compatibility is now added by Flywheel itself through mixins.
 */
@Mixin(value = FlywheelCompat.class, remap = false)
public class FlywheelCompatMixin {
	@Overwrite
	public static boolean addAndFilterBEs(BlockEntity blockEntity) {
		return true;
	}

	@Overwrite
	public static void filterBlockEntityList(Collection<BlockEntity> blockEntities) {
	}
}
