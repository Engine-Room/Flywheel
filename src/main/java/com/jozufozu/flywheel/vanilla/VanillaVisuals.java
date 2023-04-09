package com.jozufozu.flywheel.vanilla;

import static com.jozufozu.flywheel.lib.visual.SimpleBlockEntityVisualizer.configure;
import static com.jozufozu.flywheel.lib.visual.SimpleEntityVisualizer.configure;

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
		configure(BlockEntityType.CHEST)
				.alwaysSkipRender()
				.factory(ChestVisual::new)
				.apply();
		configure(BlockEntityType.ENDER_CHEST)
				.alwaysSkipRender()
				.factory(ChestVisual::new)
				.apply();
		configure(BlockEntityType.TRAPPED_CHEST)
				.alwaysSkipRender()
				.factory(ChestVisual::new)
				.apply();

		configure(BlockEntityType.BELL)
				.alwaysSkipRender()
				.factory(BellVisual::new)
				.apply();

		configure(BlockEntityType.SHULKER_BOX)
				.alwaysSkipRender()
				.factory(ShulkerBoxVisual::new)
				.apply();

		configure(EntityType.MINECART)
				.skipRender(MinecartVisual::shouldSkipRender)
				.factory(MinecartVisual::new)
				.apply();
		configure(EntityType.COMMAND_BLOCK_MINECART)
				.skipRender(MinecartVisual::shouldSkipRender)
				.factory(MinecartVisual::new)
				.apply();
		configure(EntityType.FURNACE_MINECART)
				.skipRender(MinecartVisual::shouldSkipRender)
				.factory(MinecartVisual::new)
				.apply();
		configure(EntityType.HOPPER_MINECART)
				.skipRender(MinecartVisual::shouldSkipRender)
				.factory(MinecartVisual::new)
				.apply();
		configure(EntityType.TNT_MINECART)
				.skipRender(MinecartVisual::shouldSkipRender)
				.factory(MinecartVisual::new)
				.apply();
	}
}
