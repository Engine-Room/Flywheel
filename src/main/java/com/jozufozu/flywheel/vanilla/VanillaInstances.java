package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * TODO:
 * <table>
 * 		<tr><td>{@link TileEntityType#SIGN}</td><td> {@link net.minecraft.client.renderer.tileentity.SignTileEntityRenderer SignTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#PISTON}</td><td> {@link net.minecraft.client.renderer.tileentity.PistonTileEntityRenderer PistonTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#CONDUIT}</td><td> {@link net.minecraft.client.renderer.tileentity.ConduitTileEntityRenderer ConduitTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#ENCHANTING_TABLE}</td><td> {@link net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer EnchantmentTableTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#LECTERN}</td><td> {@link net.minecraft.client.renderer.tileentity.LecternTileEntityRenderer LecternTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#MOB_SPAWNER}</td><td> {@link net.minecraft.client.renderer.tileentity.MobSpawnerTileEntityRenderer MobSpawnerTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BED}</td><td> {@link net.minecraft.client.renderer.tileentity.BedTileEntityRenderer BedTileEntityRenderer}</td></tr>
 * 		<tr><td>^^ Interesting - Major vv</td></tr>
 * 		<tr><td>{@link TileEntityType#END_PORTAL}</td><td> {@link net.minecraft.client.renderer.tileentity.EndPortalTileEntityRenderer EndPortalTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#END_GATEWAY}</td><td> {@link net.minecraft.client.renderer.tileentity.EndGatewayTileEntityRenderer EndGatewayTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BEACON}</td><td> {@link net.minecraft.client.renderer.tileentity.BeaconTileEntityRenderer BeaconTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#SKULL}</td><td> {@link net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer SkullTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#BANNER}</td><td> {@link net.minecraft.client.renderer.tileentity.BannerTileEntityRenderer BannerTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#STRUCTURE_BLOCK}</td><td> {@link net.minecraft.client.renderer.tileentity.StructureTileEntityRenderer StructureTileEntityRenderer}</td></tr>
 * 		<tr><td>{@link TileEntityType#CAMPFIRE}</td><td> {@link net.minecraft.client.renderer.tileentity.CampfireTileEntityRenderer CampfireTileEntityRenderer}</td></tr>
 * </table>
 */
public class VanillaInstances {

	public static void init() {
		InstancedRenderRegistry r = InstancedRenderRegistry.getInstance();

		r.tile(BlockEntityType.CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);
		r.tile(BlockEntityType.ENDER_CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);
		r.tile(BlockEntityType.TRAPPED_CHEST)
				.setSkipRender(true)
				.factory(ChestInstance::new);

		r.tile(BlockEntityType.BELL)
				.setSkipRender(true)
				.factory(BellInstance::new);

		r.tile(BlockEntityType.SHULKER_BOX)
				.setSkipRender(true)
				.factory(ShulkerBoxInstance::new);

		r.entity(EntityType.MINECART)
				.setSkipRender(true)
				.factory(MinecartInstance::new);
		r.entity(EntityType.HOPPER_MINECART)
				.setSkipRender(true)
				.factory(MinecartInstance::new);
		r.entity(EntityType.FURNACE_MINECART)
				.setSkipRender(true)
				.factory(MinecartInstance::new);
	}
}
