package dev.engine_room.flywheel.lib.model.baked;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.MapMaker;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

@ApiStatus.Internal
public class FabricPartialModel extends PartialModel {
	private static final ConcurrentMap<Identifier, FabricPartialModel> ALL = new MapMaker().weakValues().makeMap();

	private final ExtraModelKey<@NotNull BlockStateModel> key;

	public FabricPartialModel(Identifier modelId) {
		super(modelId);
		key = ExtraModelKey.create(modelId::toString);
	}

	public static FabricPartialModel of(Identifier modelId) {
		return ALL.computeIfAbsent(modelId, FabricPartialModel::new);
	}

	public static void registerAll(ModelLoadingPlugin.Context ctx) {
		for (FabricPartialModel partial : ALL.values()) {
			ctx.addModel(partial.key, partial.new Unbaked());
		}
	}

	public static void refreshAll(ModelManager modelManager) {
		for (FabricPartialModel partial : ALL.values()) {
			partial.blockStateModel = modelManager.getModel(partial.key);
		}
	}

	private class Unbaked implements UnbakedExtraModel<@NotNull BlockStateModel> {
		@Override
		public BlockStateModel bake(ModelBaker baker) {
			return new SingleVariant(SimpleModelWrapper.bake(baker, modelId, BlockModelRotation.IDENTITY));
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			resolver.markDependency(modelId);
		}
	}

	public static class ResourceReloadListener implements ResourceManagerReloadListener {
		public static final Identifier ID = IdentifierUtil.id("partial_models");

		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			refreshAll(Minecraft.getInstance().getModelManager());
		}
	}
}
