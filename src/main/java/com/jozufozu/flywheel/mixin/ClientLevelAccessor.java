package com.jozufozu.flywheel.mixin;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.world.entity.Entity;

import net.minecraft.world.level.entity.TransientEntitySectionManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientLevel.class)
public interface ClientLevelAccessor {
	@Accessor("entityStorage")
	TransientEntitySectionManager<Entity> getEntityStorage();
}
