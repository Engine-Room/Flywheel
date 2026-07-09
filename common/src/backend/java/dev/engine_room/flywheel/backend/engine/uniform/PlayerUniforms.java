package dev.engine_room.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.backend.FlwBackendXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public final class PlayerUniforms extends UniformWriter {
	private static final int SIZE = 16 * 2 + 8 + 4 * 9;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.PLAYER_INDEX, SIZE);

	private PlayerUniforms() {
	}

	public static void update(RenderContext context) {
		Minecraft minecraft = Minecraft.getInstance();
		Entity cameraEntity = minecraft.getCameraEntity();
		if (!(cameraEntity instanceof Player player)) {
			BUFFER.clear();
			return;
		}

		long ptr = BUFFER.ptr();

		PlayerInfo info = minecraft.getConnection() == null ? null : minecraft.getConnection().getPlayerInfo(player.getUUID());

		Vec3 eyePos = player.getEyePosition(context.partialTick());
		ptr = writeVec3(ptr, (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);

		ptr = writeTeamColor(ptr, info == null ? null : info.getTeam());

		ptr = writeEyeBrightness(ptr, player);

		ptr = writeHeldLight(ptr, player);
		ptr = writeEyeIn(ptr, player);

		ptr = writeInt(ptr, player.isCrouching() ? 1 : 0);
		ptr = writeInt(ptr, player.isSleeping() ? 1 : 0);
		ptr = writeInt(ptr, player.isSwimming() ? 1 : 0);
		ptr = writeInt(ptr, player.isFallFlying() ? 1 : 0);

		ptr = writeInt(ptr, player.isShiftKeyDown() ? 1 : 0);

		ptr = writeInt(ptr, info == null ? 0 : info.getGameMode()
				.getId());

		BUFFER.markDirty();
	}

	private static long writeTeamColor(long ptr, @Nullable PlayerTeam team) {
		if (team != null) {
			Integer color = team.getColor()
					.map(net.minecraft.world.scores.TeamColor::rgb)
					.orElse(null);

			if (color != null) {
				int red = ARGB.red(color);
				int green = ARGB.green(color);
				int blue = ARGB.blue(color);
				return writeVec4(ptr, red / 255f, green / 255f, blue / 255f, 1f);
			} else {
				return writeVec4(ptr, 1f, 1f, 1f, 1f);
			}
		} else {
			return writeVec4(ptr, 1f, 1f, 1f, 0f);
		}
	}

	private static long writeEyeBrightness(long ptr, Player player) {
		ClientLevel level = (ClientLevel) player.level();
		int blockBrightness = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
		int skyBrightness = level.getBrightness(LightLayer.SKY, player.blockPosition());
		int maxBrightness = Level.MAX_BRIGHTNESS;

		return writeVec2(ptr, (float) blockBrightness / (float) maxBrightness,
				(float) skyBrightness / (float) maxBrightness);
	}

	private static long writeHeldLight(long ptr, Player player) {
		int heldLight = 0;

		for (InteractionHand hand : InteractionHand.values()) {
			Item handItem = player.getItemInHand(hand).getItem();
			if (handItem instanceof BlockItem blockItem) {
				Block block = blockItem.getBlock();
				ClientLevel level = (ClientLevel) player.level();
				int blockLight = FlwBackendXplat.INSTANCE
						.getLightEmission(block.defaultBlockState(), level, player.blockPosition());
				if (heldLight < blockLight) {
					heldLight = blockLight;
				}
			}
		}

		return writeFloat(ptr, (float) heldLight / 15);
	}

	private static long writeEyeIn(long ptr, Player player) {
		ClientLevel level = (ClientLevel) player.level();
		Vec3 eyePos = player.getEyePosition();
		BlockPos blockPos = BlockPos.containing(eyePos);
		return writeInFluidAndBlock(ptr, level, blockPos, eyePos);
	}
}
