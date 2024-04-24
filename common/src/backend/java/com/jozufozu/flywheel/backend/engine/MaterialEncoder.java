package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.material.DepthTest;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.backend.ShaderIndices;

import net.minecraft.util.Mth;

// Materials are unpacked in "flywheel:flywheel/internal/packed_material.glsl"
public final class MaterialEncoder {
	// The number of bits each property takes up
	private static final int BLUR_LENGTH = 1;
	private static final int MIPMAP_LENGTH = 1;
	private static final int BACKFACE_CULLING_LENGTH = 1;
	private static final int POLYGON_OFFSET_LENGTH = 1;
	private static final int DEPTH_TEST_LENGTH = Mth.ceillog2(DepthTest.values().length);
	private static final int TRANSPARENCY_LENGTH = Mth.ceillog2(Transparency.values().length);
	private static final int WRITE_MASK_LENGTH = Mth.ceillog2(WriteMask.values().length);
	private static final int USE_OVERLAY_LENGTH = 1;
	private static final int USE_LIGHT_LENGTH = 1;
	private static final int DIFFUSE_LENGTH = 1;

	// The bit offset of each property
	private static final int BLUR_OFFSET = 0;
	private static final int MIPMAP_OFFSET = BLUR_OFFSET + BLUR_LENGTH;
	private static final int BACKFACE_CULLING_OFFSET = MIPMAP_OFFSET + MIPMAP_LENGTH;
	private static final int POLYGON_OFFSET_OFFSET = BACKFACE_CULLING_OFFSET + BACKFACE_CULLING_LENGTH;
	private static final int DEPTH_TEST_OFFSET = POLYGON_OFFSET_OFFSET + POLYGON_OFFSET_LENGTH;
	private static final int TRANSPARENCY_OFFSET = DEPTH_TEST_OFFSET + DEPTH_TEST_LENGTH;
	private static final int WRITE_MASK_OFFSET = TRANSPARENCY_OFFSET + TRANSPARENCY_LENGTH;
	private static final int USE_OVERLAY_OFFSET = WRITE_MASK_OFFSET + WRITE_MASK_LENGTH;
	private static final int USE_LIGHT_OFFSET = USE_OVERLAY_OFFSET + USE_OVERLAY_LENGTH;
	private static final int DIFFUSE_OFFSET = USE_LIGHT_OFFSET + USE_LIGHT_LENGTH;

	// The bit mask for each property
	private static final int BLUR_MASK = bitMask(BLUR_LENGTH, BLUR_OFFSET);
	private static final int MIPMAP_MASK = bitMask(MIPMAP_LENGTH, MIPMAP_OFFSET);
	private static final int BACKFACE_CULLING_MASK = bitMask(BACKFACE_CULLING_LENGTH, BACKFACE_CULLING_OFFSET);
	private static final int POLYGON_OFFSET_MASK = bitMask(POLYGON_OFFSET_LENGTH, POLYGON_OFFSET_OFFSET);
	private static final int DEPTH_TEST_MASK = bitMask(DEPTH_TEST_LENGTH, DEPTH_TEST_OFFSET);
	private static final int TRANSPARENCY_MASK = bitMask(TRANSPARENCY_LENGTH, TRANSPARENCY_OFFSET);
	private static final int WRITE_MASK_MASK = bitMask(WRITE_MASK_LENGTH, WRITE_MASK_OFFSET);
	private static final int USE_OVERLAY_MASK = bitMask(USE_OVERLAY_LENGTH, USE_OVERLAY_OFFSET);
	private static final int USE_LIGHT_MASK = bitMask(USE_LIGHT_LENGTH, USE_LIGHT_OFFSET);
	private static final int DIFFUSE_MASK = bitMask(DIFFUSE_LENGTH, DIFFUSE_OFFSET);

	private MaterialEncoder() {
	}

	private static int bitMask(int bitLength, int bitOffset) {
		return ((1 << bitLength) - 1) << bitOffset;
	}

	public static int packFogAndCutout(Material material) {
		var fog = ShaderIndices.fog()
				.index(material.fog()
						.source());
		var cutout = ShaderIndices.cutout()
				.index(material.cutout()
						.source());

		return fog & 0xFFFF | (cutout & 0xFFFF) << 16;
	}

	// Packed format:
	// diffuse[1] | useLight[1] | useOverlay[1] | writeMask[2] | transparency[3] | depthTest[4] | polygonOffset[1] | backfaceCulling[1] | mipmap[1] | blur[1]
	public static int packProperties(Material material) {
		int bits = 0;

		if (material.blur()) bits |= BLUR_MASK;
		if (material.mipmap()) bits |= MIPMAP_MASK;
		if (material.backfaceCulling()) bits |= BACKFACE_CULLING_MASK;
		if (material.polygonOffset()) bits |= POLYGON_OFFSET_MASK;
		bits |= (material.depthTest().ordinal() << DEPTH_TEST_OFFSET) & DEPTH_TEST_MASK;
		bits |= (material.transparency().ordinal() << TRANSPARENCY_OFFSET) & TRANSPARENCY_MASK;
		bits |= (material.writeMask().ordinal() << WRITE_MASK_OFFSET) & WRITE_MASK_MASK;
		if (material.useOverlay()) bits |= USE_OVERLAY_MASK;
		if (material.useLight()) bits |= USE_LIGHT_MASK;
		if (material.diffuse()) bits |= DIFFUSE_MASK;

		return bits;
	}
}
