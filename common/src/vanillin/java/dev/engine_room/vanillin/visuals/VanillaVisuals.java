package dev.engine_room.vanillin.visuals;

import static dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer.builder;
import static dev.engine_room.flywheel.lib.visualization.SimpleEntityVisualizer.builder;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VanillaVisuals {
	public static void init() {
		builder(BlockEntityType.CHEST)
				.factory(ChestVisual::new)
				.apply();
		builder(BlockEntityType.ENDER_CHEST)
				.factory(ChestVisual::new)
				.apply();
		builder(BlockEntityType.TRAPPED_CHEST)
				.factory(ChestVisual::new)
				.apply();

		builder(BlockEntityType.BELL)
				.factory(BellVisual::new)
				.apply();

		builder(BlockEntityType.SHULKER_BOX)
				.factory(ShulkerBoxVisual::new)
				.apply();

		builder(EntityType.CHEST_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.CHEST_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.COMMAND_BLOCK_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.COMMAND_BLOCK_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.FURNACE_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.FURNACE_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.HOPPER_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.HOPPER_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.SPAWNER_MINECART)
				.factory((ctx, entity, partialTick) -> new MinecartVisual<>(ctx, entity, partialTick, ModelLayers.SPAWNER_MINECART))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.TNT_MINECART)
				.factory(TntMinecartVisual::new)
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
	}
}
