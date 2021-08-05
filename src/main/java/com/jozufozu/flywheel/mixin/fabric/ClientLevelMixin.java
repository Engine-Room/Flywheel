package com.jozufozu.flywheel.mixin.fabric;

import java.util.function.Supplier;

import net.minecraft.client.multiplayer.ClientLevel;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import net.minecraft.world.level.storage.WritableLevelData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.event.ForgeEvents;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInitTail(CallbackInfo ci) {
		ForgeEvents.onLoadWorld((ClientLevel) (Object) this);
	}

	protected ClientLevelMixin(WritableLevelData spawnWorldInfo, ResourceKey<Level> registryKey, DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l) {
		super(spawnWorldInfo, registryKey, dimensionType, supplier, bl, bl2, l);
	}
}
