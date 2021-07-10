package com.jozufozu.flywheel.mixin.fabric;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.event.ForgeEvents;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.storage.ISpawnWorldInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInitTail(CallbackInfo ci) {
		ForgeEvents.onLoadWorld((ClientWorld) (Object) this);
	}

	protected ClientWorldMixin(ISpawnWorldInfo spawnWorldInfo, RegistryKey<World> registryKey, DimensionType dimensionType, Supplier<IProfiler> supplier, boolean bl, boolean bl2, long l) {
		super(spawnWorldInfo, registryKey, dimensionType, supplier, bl, bl2, l);
	}
}
