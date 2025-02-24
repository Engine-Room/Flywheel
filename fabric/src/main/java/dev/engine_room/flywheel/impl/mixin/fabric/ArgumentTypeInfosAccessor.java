package dev.engine_room.flywheel.impl.mixin.fabric;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;

@Mixin(ArgumentTypeInfos.class)
public interface ArgumentTypeInfosAccessor {
	@Accessor("BY_CLASS")
	static Map<Class<?>, ArgumentTypeInfo<?, ?>> getBY_CLASS() {
		throw new AssertionError();
	}
}
