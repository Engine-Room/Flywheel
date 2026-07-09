package dev.engine_room.flywheel.lib.util;

import net.minecraft.client.renderer.Lightmap;
import net.minecraft.world.level.dimension.DimensionType;

public final class LightUtil {
	public static final int FULL_BLOCK = pack(15, 0);
	public static final int FULL_BRIGHT = pack(15, 15);

	private LightUtil() {
	}

	public static int pack(int blockLight, int skyLight) {
		return blockLight << 4 | skyLight << 20;
	}

	public static int block(int packedLight) {
		return packedLight >> 4 & 15;
	}

	public static int sky(int packedLight) {
		return packedLight >> 20 & 15;
	}

	public static float getBrightness(DimensionType dimensionType, int lightLevel) {
		return Lightmap.getBrightness(dimensionType, lightLevel);
	}
}
