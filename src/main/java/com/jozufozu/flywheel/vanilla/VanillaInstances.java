package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.tileentity.TileEntityType;

/**
 * TODO:
 * <table>
 * 		<tr><td>{@link TileEntityType#SIGN}</td><td> {@link net.minecraft.client.renderer.tileentity.SignTileEntityRenderer SignTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#MOB_SPAWNER}</td><td> {@link net.minecraft.client.renderer.tileentity.MobSpawnerTileEntityRenderer MobSpawnerTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#PISTON}</td><td> {@link net.minecraft.client.renderer.tileentity.PistonTileEntityRenderer PistonTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#ENCHANTING_TABLE}</td><td> {@link net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer EnchantmentTableTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#LECTERN}</td><td> {@link net.minecraft.client.renderer.tileentity.LecternTileEntityRenderer LecternTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#END_PORTAL}</td><td> {@link net.minecraft.client.renderer.tileentity.EndPortalTileEntityRenderer EndPortalTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#END_GATEWAY}</td><td> {@link net.minecraft.client.renderer.tileentity.EndGatewayTileEntityRenderer EndGatewayTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BEACON}</td><td> {@link net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer BeaconTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#SKULL}</td><td> {@link net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer SkullTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BANNER}</td><td> {@link net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer BannerTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#STRUCTURE_BLOCK}</td><td> {@link net.minecraft.client.renderer.tileentity.StructureTileEntityRenderer StructureTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#SHULKER_BOX}</td><td> {@link net.minecraft.client.renderer.tileentity.ShulkerBoxTileEntityRenderer ShulkerBoxTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BED}</td><td> {@link net.minecraft.client.renderer.tileentity.BedTileEntityRenderer BedTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#CONDUIT}</td><td> {@link net.minecraft.client.renderer.tileentity.ConduitTileEntityRenderer ConduitTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BELL}</td><td> {@link net.minecraft.client.renderer.tileentity.BellTileEntityRenderer BellTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#CAMPFIRE}</td><td> {@link net.minecraft.client.renderer.tileentity.CampfireTileEntityRenderer CampfireTileEntityRenderer}</td></tr>
 * </table>
 */
public class VanillaInstances {

	public static void init() {
		InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();

		r.tile(TileEntityType.CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);
		r.tile(TileEntityType.ENDER_CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);
		r.tile(TileEntityType.TRAPPED_CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);

		r.tile(TileEntityType.BELL)
				.setSkipRender(true)
				.factory(BellInstance::new);
	}
}
