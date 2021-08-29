package com.jozufozu.flywheel.light;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class Volume {

	public static Volume.Block block(BlockPos pos) {
		return new Block(pos);
	}

	public static Volume.Box box(GridAlignedBB box) {
		return new Box(box);
	}

	public static Volume.Box box(AxisAlignedBB box) {
		return new Box(GridAlignedBB.from(box));
	}

	public static class Block extends Volume {
		public final BlockPos pos;

		public Block(BlockPos pos) {
			this.pos = pos;
		}
	}

	public static class Box extends Volume {
		public final GridAlignedBB box;

		public Box(GridAlignedBB box) {
			this.box = box;
		}
	}
}
