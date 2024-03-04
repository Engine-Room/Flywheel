package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class LevelUniforms implements UniformProvider {
	public static final int SIZE = 12 * 4 + 2 * 16;

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
		ClientLevel level = mc.level;
		if (level == null || context == null) {
			MemoryUtil.memSet(ptr, 0, SIZE);
			return;
		}

		float ptick = context.partialTick();

		Vec3 skyColor = level.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), ptick);
		ptr = Uniforms.writeVec4(ptr, (float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1f);

		Vec3 cloudColor = level.getCloudColor(ptick);
		ptr = Uniforms.writeVec4(ptr, (float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1f);

		long dayTime = level.getDayTime();
		long levelDay = dayTime / 24000L;
		long timeOfDay = dayTime - levelDay * 24000L;
		MemoryUtil.memPutInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
		ptr += 4;
		MemoryUtil.memPutFloat(ptr, (float) timeOfDay / 24000f);
		ptr += 4;

		MemoryUtil.memPutInt(ptr, level.dimensionType().hasSkyLight() ? 1 : 0);
		ptr += 4;

		float sunAngle = level.getSunAngle(ptick);
		MemoryUtil.memPutFloat(ptr, sunAngle);
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, level.getMoonBrightness());
		ptr += 4;
		MemoryUtil.memPutInt(ptr, level.getMoonPhase());
		ptr += 4;

		MemoryUtil.memPutInt(ptr, level.isRaining() ? 1 : 0);
		ptr += 4;
		MemoryUtil.memPutFloat(ptr, level.getRainLevel(ptick));
		ptr += 4;

		MemoryUtil.memPutInt(ptr, level.isThundering() ? 1 : 0);
		ptr += 4;
		MemoryUtil.memPutFloat(ptr, level.getThunderLevel(ptick));
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, level.getSkyDarken(ptick));
		ptr += 4;

		// TODO: use defines for custom dimension ids
        int dimensionId;
        ResourceKey<Level> dimension = level.dimension();
        if (Level.OVERWORLD.equals(dimension)) {
            dimensionId = 0;
        } else if (Level.NETHER.equals(dimension)) {
            dimensionId = 1;
        } else if (Level.END.equals(dimension)) {
            dimensionId = 2;
        } else {
            dimensionId = -1;
        }
		MemoryUtil.memPutInt(ptr, dimensionId);
    }
}
