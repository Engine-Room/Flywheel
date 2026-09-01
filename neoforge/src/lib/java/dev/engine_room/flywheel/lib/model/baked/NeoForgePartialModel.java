package dev.engine_room.flywheel.lib.model.baked;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.MapMaker;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;

import org.jspecify.annotations.NullMarked;

@ApiStatus.Internal
public class NeoForgePartialModel extends PartialModel {
	private static final ConcurrentMap<Identifier, NeoForgePartialModel> ALL = new MapMaker().weakValues().makeMap();

	private final StandaloneModelKey<BlockStateModel> key;

	public NeoForgePartialModel(Identifier modelId) {
		super(modelId);
		key = new StandaloneModelKey<>(modelId::toString);
	}

	public static NeoForgePartialModel of(Identifier modelId) {
		return ALL.computeIfAbsent(modelId, NeoForgePartialModel::new);
	}

	public static void registerAll(ModelEvent.RegisterStandalone event) {
		for (NeoForgePartialModel partial : ALL.values()) {
			event.register(partial.key, partial.new Unbaked());
		}
	}

	public static void refreshAll(ModelEvent.BakingCompleted event) {
		ModelManager modelManager = event.getModelManager();

		for (NeoForgePartialModel partial : ALL.values()) {
			partial.blockStateModel = modelManager.getStandaloneModel(partial.key);
		}
	}

	@NullMarked
	private class Unbaked implements UnbakedStandaloneModel<BlockStateModel> {
		@Override
		public BlockStateModel bake(ModelBaker baker, ModelDebugName name) {
			return new SingleVariant(SimpleModelWrapper.bake(baker, modelId, BlockModelRotation.IDENTITY));
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			resolver.markDependency(modelId);
		}
	}
}
