package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.backend.mixin.AbstractClientPlayerAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;

public class PlayerUniforms implements UniformProvider {
	public static final int SIZE = 9 * 4 + 8 + 2 * 16;

	@Nullable
	private RenderContext context;

	@Override
	public int byteSize() {
		return SIZE;
	}

	public void setContext(RenderContext context) {
		this.context = context;
	}

	@Override
	public void write(long ptr) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (context == null || player == null) {
			MemoryUtil.memSet(ptr, 0, SIZE);
			return;
		}

		int luminance = 0;
		for (InteractionHand hand : InteractionHand.values()) {
			Item handItem = player.getItemInHand(hand).getItem();
			if (handItem instanceof BlockItem bitem) {
				Block block = bitem.getBlock();
				int blockLight = block.defaultBlockState().getLightEmission(player.clientLevel, BlockPos.ZERO);
				if (luminance < blockLight) {
					luminance = blockLight;
				}
			}
		}

		MemoryUtil.memPutFloat(ptr, (float) luminance / 15);
		ptr += 4;

		Vec3 eyePos = player.getEyePosition(context.partialTick());
		ptr = Uniforms.writeVec3(ptr, (float) eyePos.x, (float) eyePos.y, (float) eyePos.z);

		int blockBrightness = player.clientLevel.getBrightness(LightLayer.BLOCK, player.blockPosition());
		int skyBrightness = player.clientLevel.getBrightness(LightLayer.SKY, player.blockPosition());
		int maxBrightness = player.clientLevel.getMaxLightLevel();

		ptr = Uniforms.writeVec2(ptr, (float) blockBrightness / (float) maxBrightness,
				(float) skyBrightness / (float) maxBrightness);

		ptr = writeEyeIn(ptr, player);

		MemoryUtil.memPutInt(ptr, player.isCrouching() ? 1 : 0);
		ptr += 4;
		MemoryUtil.memPutInt(ptr, player.isSleeping() ? 1 : 0);
		ptr += 4;
		MemoryUtil.memPutInt(ptr, player.isSwimming() ? 1 : 0);
		ptr += 4;
		MemoryUtil.memPutInt(ptr, player.isFallFlying() ? 1 : 0);
		ptr += 4;

		MemoryUtil.memPutInt(ptr, player.isShiftKeyDown() ? 1 : 0);
		ptr += 4;

		PlayerInfo info = ((AbstractClientPlayerAccessor) player).flywheel$getPlayerInfo();
		MemoryUtil.memPutInt(ptr, info.getGameMode().getId());
		ptr += 4;

		int red = 0, green = 0, blue = 0, alpha = 0;
		PlayerTeam team = info.getTeam();
		if (team != null) {
			Integer color = team.getColor().getColor();
			if (color != null) {
				int icolor = color;
				red = FastColor.ARGB32.red(icolor);
				green = FastColor.ARGB32.green(icolor);
				blue = FastColor.ARGB32.blue(icolor);
				alpha = 255;
			}
		}

		ptr = Uniforms.writeVec4(ptr, (float) red / 255f, (float) blue / 255f, (float) green / 255f,
				(float) alpha / 255f);
	}

	private long writeEyeIn(long ptr, LocalPlayer player) {
		ClientLevel level = player.clientLevel;
		Vec3 eyePos = player.getEyePosition();
		BlockPos blockPos = BlockPos.containing(eyePos);
		return Uniforms.writeInFluidAndBlock(ptr, level, blockPos, eyePos);
	}
}
