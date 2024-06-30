package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.lib.util.ExtraMemoryOps;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

class UniformWriter {
	static long writeInt(long ptr, int value) {
		MemoryUtil.memPutInt(ptr, value);
		return ptr + 4;
	}

	static long writeFloat(long ptr, float value) {
		MemoryUtil.memPutFloat(ptr, value);
		return ptr + 4;
	}

	static long writeVec2(long ptr, float x, float y) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		return ptr + 8;
	}

	static long writeVec3(long ptr, float x, float y, float z) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		MemoryUtil.memPutFloat(ptr + 12, 0f); // empty component of vec4 because we don't trust std140
		return ptr + 16;
	}

	static long writeVec4(long ptr, float x, float y, float z, float w) {
		MemoryUtil.memPutFloat(ptr, x);
		MemoryUtil.memPutFloat(ptr + 4, y);
		MemoryUtil.memPutFloat(ptr + 8, z);
		MemoryUtil.memPutFloat(ptr + 12, w);
		return ptr + 16;
	}

	static long writeMat4(long ptr, Matrix4f mat) {
		ExtraMemoryOps.putMatrix4f(ptr, mat);
		return ptr + 64;
	}

	static long writeInFluidAndBlock(long ptr, Level level, BlockPos blockPos, Vec3 pos) {
		FluidState fluidState = level.getFluidState(blockPos);
		BlockState blockState = level.getBlockState(blockPos);
		float height = fluidState.getHeight(level, blockPos);

		if (fluidState.isEmpty()) {
			MemoryUtil.memPutInt(ptr, 0);
		} else if (pos.y < blockPos.getY() + height) {
			// TODO: handle custom fluids via defines
			if (fluidState.is(FluidTags.WATER)) {
				MemoryUtil.memPutInt(ptr, 1);
			} else if (fluidState.is(FluidTags.LAVA)) {
				MemoryUtil.memPutInt(ptr, 2);
			} else {
				MemoryUtil.memPutInt(ptr, -1);
			}
		}

		if (blockState.isAir()) {
			MemoryUtil.memPutInt(ptr + 4, 0);
		} else {
			// TODO: handle custom blocks via defines
			if (blockState.is(Blocks.POWDER_SNOW)) {
				MemoryUtil.memPutInt(ptr + 4, 0);
			} else {
				MemoryUtil.memPutInt(ptr + 4, -1);
			}
		}

		return ptr + 8;
	}
}
