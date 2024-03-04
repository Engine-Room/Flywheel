package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public class PlayerUniforms implements UniformProvider {
	public static final int SIZE = 4 + 8 + 16;

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
	}
}
