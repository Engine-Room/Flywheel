package dev.engine_room.flywheel.backend.engine.uniform;

import dev.engine_room.flywheel.api.RenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class LevelUniforms extends UniformWriter {
	private static final int SIZE = 16 * 2 + 4 * 13;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);

	private LevelUniforms() {
	}

	public static void update(RenderContext context) {
		long ptr = BUFFER.ptr();

		ClientLevel level = context.level();
		float partialTick = context.partialTick();

		Vec3 skyColor = level.getSkyColor(context.camera().getPosition(), partialTick);
		Vec3 cloudColor = level.getCloudColor(partialTick);
		ptr = writeVec4(ptr, (float) skyColor.x, (float) skyColor.y, (float) skyColor.z, 1f);
		ptr = writeVec4(ptr, (float) cloudColor.x, (float) cloudColor.y, (float) cloudColor.z, 1f);

		long dayTime = level.getDayTime();
		long levelDay = dayTime / 24000L;
		float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
		ptr = writeInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
		ptr = writeFloat(ptr, timeOfDay);

		ptr = writeInt(ptr, level.dimensionType().hasSkyLight() ? 1 : 0);

		ptr = writeFloat(ptr, level.getSunAngle(partialTick));

		ptr = writeFloat(ptr, level.getMoonBrightness());
		ptr = writeInt(ptr, level.getMoonPhase());

		ptr = writeInt(ptr, level.isRaining() ? 1 : 0);
		ptr = writeFloat(ptr, level.getRainLevel(partialTick));
		ptr = writeInt(ptr, level.isThundering() ? 1 : 0);
		ptr = writeFloat(ptr, level.getThunderLevel(partialTick));

		ptr = writeFloat(ptr, level.getSkyDarken(partialTick));

		ptr = writeInt(ptr, level.effects().constantAmbientLight() ? 1 : 0);

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
        ptr = writeInt(ptr, dimensionId);

		BUFFER.markDirty();
    }
}
