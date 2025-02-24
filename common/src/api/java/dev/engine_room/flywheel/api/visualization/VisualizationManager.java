package dev.engine_room.flywheel.api.visualization;

import java.util.SortedSet;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.api.internal.FlwApiLink;
import dev.engine_room.flywheel.api.visual.Effect;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

@ApiStatus.NonExtendable
public interface VisualizationManager {
	static boolean supportsVisualization(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.supportsVisualization(level);
	}

	@Nullable
	static VisualizationManager get(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.getVisualizationManager(level);
	}

	static VisualizationManager getOrThrow(@Nullable LevelAccessor level) {
		return FlwApiLink.INSTANCE.getVisualizationManagerOrThrow(level);
	}

	Vec3i renderOrigin();

	VisualManager<BlockEntity> blockEntities();

	VisualManager<Entity> entities();

	VisualManager<Effect> effects();

	/**
	 * Get the render dispatcher, which can be used to invoke rendering.
	 * <b>This should only be used by mods which heavily rewrite rendering to restore compatibility with Flywheel
	 * without mixins.</b>
	 */
	RenderDispatcher renderDispatcher();

	@ApiStatus.NonExtendable
	interface RenderDispatcher {
		/**
		 * Prepare visuals for render.
		 *
		 * <p>Guaranteed to be called before {@link #afterEntities} and {@link #beforeCrumbling}.
		 * <br>Guaranteed to be called after the render thread has processed all light updates.
		 * <br>The caller is otherwise free to choose an invocation site, but it is recommended to call
		 * this as early as possible to give the VisualizationManager time to process things off-thread.
		 */
		void onStartLevelRender(RenderContext ctx);

		/**
		 * Render instances.
		 *
		 * <p>Guaranteed to be called after {@link #onStartLevelRender} and before {@link #beforeCrumbling}.
		 * <br>The caller is otherwise free to choose an invocation site, but it is recommended to call
		 * this between rendering entities and block entities.
		 */
		void afterEntities(RenderContext ctx);

		/**
		 * Render crumbling block entities.
		 *
		 * <p>Guaranteed to be called after {@link #onStartLevelRender} and {@link #afterEntities}
		 * @param destructionProgress The destruction progress map from {@link net.minecraft.client.renderer.LevelRenderer LevelRenderer}.
		 */
		void beforeCrumbling(RenderContext ctx, Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress);
	}
}
