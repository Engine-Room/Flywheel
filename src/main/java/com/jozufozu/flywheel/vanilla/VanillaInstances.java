package com.jozufozu.flywheel.vanilla;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * TODO:
 * <table>
 * 		<tr><td>{@link BlockEntityType#SIGN}</td><td> {@link net.minecraft.client.renderer.blockentity.SignRenderer SignBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#PISTON}</td><td> {@link net.minecraft.client.renderer.blockentity.PistonHeadRenderer PistonBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#CONDUIT}</td><td> {@link net.minecraft.client.renderer.blockentity.ConduitRenderer ConduitBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#ENCHANTING_TABLE}</td><td> {@link net.minecraft.client.renderer.blockentity.EnchantTableRenderer EnchantmentTableBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#LECTERN}</td><td> {@link net.minecraft.client.renderer.blockentity.LecternRenderer LecternBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#MOB_SPAWNER}</td><td> {@link net.minecraft.client.renderer.blockentity.SpawnerRenderer MobSpawnerBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BED}</td><td> {@link net.minecraft.client.renderer.blockentity.BedRenderer BedBlockEntityRenderer}</td></tr>
 * 		<tr><td>^^ Interesting - Major vv</td></tr>
 * 		<tr><td>{@link BlockEntityType#END_PORTAL}</td><td> {@link net.minecraft.client.renderer.blockentity.TheEndPortalRenderer EndPortalBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#END_GATEWAY}</td><td> {@link net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer EndGatewayBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BEACON}</td><td> {@link net.minecraft.client.renderer.blockentity.BeaconRenderer BeaconBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#SKULL}</td><td> {@link net.minecraft.client.renderer.blockentity.SkullBlockRenderer SkullBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#BANNER}</td><td> {@link net.minecraft.client.renderer.blockentity.BannerRenderer BannerBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#STRUCTURE_BLOCK}</td><td> {@link net.minecraft.client.renderer.blockentity.StructureBlockRenderer StructureBlockEntityRenderer}</td></tr>
 * 		<tr><td>{@link BlockEntityType#CAMPFIRE}</td><td> {@link net.minecraft.client.renderer.blockentity.CampfireRenderer CampfireBlockEntityRenderer}</td></tr>
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
	}
}
