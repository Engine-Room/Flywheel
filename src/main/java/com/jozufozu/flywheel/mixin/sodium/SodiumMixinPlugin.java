package com.jozufozu.flywheel.mixin.sodium;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.common.base.Suppliers;
import com.jozufozu.flywheel.Flywheel;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

public class SodiumMixinPlugin implements IMixinConfigPlugin {
	private static final Supplier<Boolean> IS_EMBEDDIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("embeddium"));

	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		boolean shouldUseMixin = false;
		try {
			VersionPredicate predicate = VersionPredicateParser.parse(">=0.5 <0.6");
			Version sodiumVersion = FabricLoader.getInstance()
					.getModContainer("sodium")
					.orElseThrow()
					.getMetadata()
					.getVersion();
			shouldUseMixin = predicate.test(sodiumVersion);
		} catch(Throwable ignored) {}

		return Flywheel.IS_SODIUM_LOADED.get() && !IS_EMBEDDIUM_LOADED.get() && shouldUseMixin;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
