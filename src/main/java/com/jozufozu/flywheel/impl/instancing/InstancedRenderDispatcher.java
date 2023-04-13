package com.jozufozu.flywheel.impl.instancing;

import java.util.List;

import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.extension.ClientLevelExtension;
import com.jozufozu.flywheel.impl.instancing.manager.InstanceManager;
import com.jozufozu.flywheel.lib.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.FlwUtil;
import com.jozufozu.flywheel.util.WorldAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;

public class InstancedRenderDispatcher {
	private static final WorldAttached<InstanceWorld> INSTANCE_WORLDS = new WorldAttached<>(InstanceWorld::new);

	/**
	 * Call this when you want to run {@link Instance#update()}.
	 * @param blockEntity The block entity whose instance you want to update.
	 */
	public static void queueUpdate(BlockEntity blockEntity) {
		if (!(blockEntity.getLevel() instanceof ClientLevel level)) {
			return;
		}

		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		INSTANCE_WORLDS.get(level)
				.getBlockEntities()
				.queueUpdate(blockEntity);
	}

	/**
	 * Call this when you want to run {@link Instance#update()}.
	 * @param entity The entity whose instance you want to update.
	 */
	public static void queueUpdate(Entity entity) {
		Level level = entity.level;
		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		INSTANCE_WORLDS.get(level)
				.getEntities()
				.queueUpdate(entity);
	}

	/**
	 * Call this when you want to run {@link Instance#update()}.
	 * @param effect The effect whose instance you want to update.
	 */
	public static void queueUpdate(LevelAccessor level, Effect effect) {
		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		INSTANCE_WORLDS.get(level)
				.getEffects()
				.queueUpdate(effect);
	}

	/**
	 * Get or create the {@link InstanceWorld} for the given world.
	 * @throws IllegalStateException if the backend is off
	 */
	private static InstanceWorld getInstanceWorld(LevelAccessor level) {
		if (!FlwUtil.canUseInstancing(level)) {
			throw new IllegalStateException("Cannot retrieve instance world when backend is off!");
		}
		return INSTANCE_WORLDS.get(level);
	}

	public static InstanceManager<BlockEntity> getBlockEntities(LevelAccessor level) {
		return getInstanceWorld(level).getBlockEntities();
	}

	public static InstanceManager<Entity> getEntities(LevelAccessor level) {
		return getInstanceWorld(level).getEntities();
	}

	public static InstanceManager<Effect> getEffects(LevelAccessor level) {
		return getInstanceWorld(level).getEffects();
	}

	public static Vec3i getRenderOrigin(LevelAccessor level) {
		return getInstanceWorld(level).getEngine().renderOrigin();
	}

	public static void tick(TickEvent.ClientTickEvent event) {
		if (!FlwUtil.isGameActive() || event.phase == TickEvent.Phase.START) {
			return;
		}

		AnimationTickHolder.tick();

		Minecraft mc = Minecraft.getInstance();
		if (mc.isPaused()) {
			return;
		}

		Entity cameraEntity = mc.getCameraEntity() == null ? mc.player : mc.getCameraEntity();
		if (cameraEntity == null) {
			return;
		}

		Level level = cameraEntity.level;
		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		double cameraX = cameraEntity.getX();
		double cameraY = cameraEntity.getEyeY();
		double cameraZ = cameraEntity.getZ();

		INSTANCE_WORLDS.get(level).tick(cameraX, cameraY, cameraZ);
	}

	public static void onBeginFrame(BeginFrameEvent event) {
		if (!FlwUtil.isGameActive()) {
			return;
		}

		ClientLevel level = event.getContext().level();
		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		INSTANCE_WORLDS.get(level).beginFrame(event.getContext());
	}

	public static void onRenderStage(RenderStageEvent event) {
		ClientLevel level = event.getContext().level();
		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		INSTANCE_WORLDS.get(level).renderStage(event.getContext(), event.getStage());
	}

	public static void resetInstanceWorld(ClientLevel level) {
		INSTANCE_WORLDS.remove(level, InstanceWorld::delete);

		if (!FlwUtil.canUseInstancing(level)) {
			return;
		}

		InstanceWorld world = INSTANCE_WORLDS.get(level);
		// Block entities are loaded while chunks are baked.
		// Entities are loaded with the level, so when chunks are reloaded they need to be re-added.
		ClientLevelExtension.getAllLoadedEntities(level)
				.forEach(world.getEntities()::queueAdd);
	}

	public static void addDebugInfo(List<String> info) {
		ClientLevel level = Minecraft.getInstance().level;
		if (FlwUtil.canUseInstancing(level)) {
			INSTANCE_WORLDS.get(level).addDebugInfo(info);
		} else {
			info.add("Disabled");
		}
	}
}
