// https://github.com/CaffeineMC/sodium-fabric/blob/e7643f4544f61180ed2f0ff4952d7daa2c1feaf4/common/src/api/java/net/caffeinemc/mods/sodium/api/blockentity/BlockEntityRenderPredicate.java
// PolyForm Shield License 1.0.0

package net.caffeinemc.mods.sodium.api.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
@ApiStatus.AvailableSince("0.6.0")
@FunctionalInterface
public interface BlockEntityRenderPredicate<T extends BlockEntity> {
    boolean shouldRender(BlockGetter blockGetter, BlockPos blockPos, T entity);
}
