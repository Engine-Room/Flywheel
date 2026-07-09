package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Vector3f;

import dev.engine_room.flywheel.api.backend.RenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class LevelUniforms extends UniformWriter {
	private static final int SIZE = 16 * 4 + 4 * 12;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.LEVEL_INDEX, SIZE);

	public static final Vector3f LIGHT0_DIRECTION = new Vector3f();
	public static final Vector3f LIGHT1_DIRECTION = new Vector3f();

	private LevelUniforms() {
	}

	public static void update(RenderContext context) {
		long ptr = BUFFER.ptr();

		ClientLevel level = context.level();
		float partialTick = context.partialTick();
		LevelRenderState renderState = dev.engine_room.flywheel.backend.mixin.LevelRendererAccessor.class.cast(context.renderer())
				.flywheel$getLevelRenderState();

		int skyColor = renderState.skyRenderState.skyColor;
		int cloudColor = renderState.cloudColor;
		ptr = writeVec4(ptr, ARGB.red(skyColor) / 255f, ARGB.green(skyColor) / 255f, ARGB.blue(skyColor) / 255f, 1f);
		ptr = writeVec4(ptr, ARGB.red(cloudColor) / 255f, ARGB.green(cloudColor) / 255f, ARGB.blue(cloudColor) / 255f, 1f);

		ptr = writeVec3(ptr, LIGHT0_DIRECTION);
		ptr = writeVec3(ptr, LIGHT1_DIRECTION);

		long dayTime = level.getOverworldClockTime();
		long levelDay = dayTime / 24000L;
		float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
		ptr = writeInt(ptr, (int) (levelDay % 0x7FFFFFFFL));
		ptr = writeFloat(ptr, timeOfDay);

		ptr = writeInt(ptr, level.dimensionType().hasSkyLight() ? 1 : 0);

		ptr = writeFloat(ptr, renderState.skyRenderState.sunAngle);

		int moonPhase = renderState.skyRenderState.moonPhase.index();
		ptr = writeFloat(ptr, DimensionType.MOON_BRIGHTNESS_PER_PHASE[moonPhase]);
		ptr = writeInt(ptr, moonPhase);

		ptr = writeInt(ptr, level.isRaining() ? 1 : 0);
		ptr = writeFloat(ptr, level.getRainLevel(partialTick));
		ptr = writeInt(ptr, level.isThundering() ? 1 : 0);
		ptr = writeFloat(ptr, level.getThunderLevel(partialTick));

		ptr = writeFloat(ptr, level.getSkyDarken());

		ptr = writeInt(ptr, level.dimensionType().ambientLight() > 0 ? 1 : 0);

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
