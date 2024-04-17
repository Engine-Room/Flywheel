package com.jozufozu.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.impl.extension.LevelExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;

@Mixin(Level.class)
abstract class LevelMixin implements LevelExtension {
	@Shadow
	protected abstract LevelEntityGetter<Entity> getEntities();

	@Override
	public Iterable<Entity> flywheel$getAllLoadedEntities() {
		return getEntities().getAll();
	}
}
