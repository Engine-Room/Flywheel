package com.jozufozu.flywheel.vanilla;

import static com.jozufozu.flywheel.lib.visual.SimpleBlockEntityVisualizer.builder;
import static com.jozufozu.flywheel.lib.visual.SimpleEntityVisualizer.builder;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * TODO:
 * <table>
 * 		<tr><td>{@link BlockEntityType#SIGN}</td><td> {@link net.minecraft.client.renderer.blockentity.SignRenderer SignRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#PISTON}</td><td> {@link net.minecraft.client.renderer.blockentity.PistonHeadRenderer PistonHeadRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#CONDUIT}</td><td> {@link net.minecraft.client.renderer.blockentity.ConduitRenderer ConduitRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#ENCHANTING_TABLE}</td><td> {@link net.minecraft.client.renderer.blockentity.EnchantTableRenderer EnchantTableRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#LECTERN}</td><td> {@link net.minecraft.client.renderer.blockentity.LecternRenderer LecternRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#MOB_SPAWNER}</td><td> {@link net.minecraft.client.renderer.blockentity.SpawnerRenderer SpawnerRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BED}</td><td> {@link net.minecraft.client.renderer.blockentity.BedRenderer BedRenderer}</td></tr>
 * 		<tr><td>^^ Interesting - Major vv</td></tr>
 * 		<tr><td>{@link BlockEntityType#END_PORTAL}</td><td> {@link net.minecraft.client.renderer.blockentity.TheEndPortalRenderer TheEndPortalRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#END_GATEWAY}</td><td> {@link net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer TheEndGatewayRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BEACON}</td><td> {@link net.minecraft.client.renderer.blockentity.BeaconRenderer BeaconRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#SKULL}</td><td> {@link net.minecraft.client.renderer.blockentity.SkullBlockRenderer SkullBlockRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BANNER}</td><td> {@link net.minecraft.client.renderer.blockentity.BannerRenderer BannerRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#STRUCTURE_BLOCK}</td><td> {@link net.minecraft.client.renderer.debug.StructureRenderer StructureRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#CAMPFIRE}</td><td> {@link net.minecraft.client.renderer.blockentity.CampfireRenderer CampfireRenderer}</td></tr>
 * </table>
 */
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
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.CHEST_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.COMMAND_BLOCK_MINECART)
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.COMMAND_BLOCK_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.FURNACE_MINECART)
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.FURNACE_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.HOPPER_MINECART)
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.HOPPER_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.MINECART)
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.STANDARD_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.SPAWNER_MINECART)
				.factory((ctx, entity) -> new MinecartVisual<>(ctx, entity, MinecartVisual.SPAWNER_BODY_MODEL))
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
		builder(EntityType.TNT_MINECART)
				.factory(TntMinecartVisual::new)
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply();
	}
}
