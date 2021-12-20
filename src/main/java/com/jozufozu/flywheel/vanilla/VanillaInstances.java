package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

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
