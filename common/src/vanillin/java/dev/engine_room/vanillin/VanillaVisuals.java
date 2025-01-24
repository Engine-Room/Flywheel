package dev.engine_room.vanillin;

import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder;
import dev.engine_room.vanillin.config.Configurator;
import dev.engine_room.vanillin.config.EntityVisualizerBuilder;
import dev.engine_room.vanillin.visuals.*;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VanillaVisuals {
	public static final Configurator CONFIGURATOR = new Configurator();

	public static void init() {
		builder(BlockEntityType.CHEST)
				.factory(ChestVisual::new)
				.apply(true);
		builder(BlockEntityType.ENDER_CHEST)
				.factory(ChestVisual::new)
				.apply(true);
		builder(BlockEntityType.TRAPPED_CHEST)
				.factory(ChestVisual::new)
				.apply(true);

		builder(BlockEntityType.BELL)
				.factory(BellVisual::new)
				.apply(true);

		builder(BlockEntityType.SHULKER_BOX)
				.factory(ShulkerBoxVisual::new)
				.apply(true);

		builder(BlockEntityType.SIGN).factory(SignVisual::new)
				.apply(false);

		builder(EntityType.CHEST_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.CHEST_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.COMMAND_BLOCK_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.COMMAND_BLOCK_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.FURNACE_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.FURNACE_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.HOPPER_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.HOPPER_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.SPAWNER_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.SPAWNER_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
		builder(EntityType.TNT_MINECART)
				.factory(TntMinecartVisual::new)
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(true);
	}

	public static <T extends BlockEntity> BlockEntityVisualizerBuilder<T> builder(BlockEntityType<T> type) {
		return new BlockEntityVisualizerBuilder<>(CONFIGURATOR, type);
	}

	public static <T extends Entity> EntityVisualizerBuilder<T> builder(EntityType<T> type) {
		return new EntityVisualizerBuilder<>(CONFIGURATOR, type);
	}
}
