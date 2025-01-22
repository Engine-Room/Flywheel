package dev.engine_room.vanillin;

import java.util.HashMap;
import java.util.Map;

import dev.engine_room.flywheel.api.visualization.BlockEntityVisualizer;
import dev.engine_room.flywheel.api.visualization.EntityVisualizer;
import dev.engine_room.flywheel.api.visualization.VisualizerRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Configurator {
	public final Map<BlockEntityType<?>, ConfiguredBlockEntity<?>> blockEntities = new HashMap<>();
	public final Map<EntityType<?>, ConfiguredEntity<?>> entities = new HashMap<>();

	public <T extends BlockEntity> void register(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
		blockEntities.put(type, new ConfiguredBlockEntity<>(type, visualizer, enabledByDefault));
	}

	public <T extends Entity> void register(EntityType<T> type, EntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
		entities.put(type, new ConfiguredEntity<>(type, visualizer, enabledByDefault));
	}

	public static class ConfiguredBlockEntity<T extends BlockEntity> {
		public final BlockEntityType<T> type;
		public final BlockEntityVisualizer<? super T> visualizer;
		private final boolean enabledByDefault;

		private ConfiguredBlockEntity(BlockEntityType<T> type, BlockEntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
			this.type = type;
			this.visualizer = visualizer;
			this.enabledByDefault = enabledByDefault;
		}

		public String configKey() {
			return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type).toString();
		}

		public boolean enabledByDefault() {
			return enabledByDefault;
		}

		public void set(boolean enabled) {
			if (enabled) {
				VisualizerRegistry.setVisualizer(type, visualizer);
			} else {
				VisualizerRegistry.setVisualizer(type, null);
			}
		}
	}

	public static class ConfiguredEntity<T extends Entity> {
		public final EntityType<T> type;
		public final EntityVisualizer<? super T> visualizer;
		private final boolean enabledByDefault;

		private ConfiguredEntity(EntityType<T> type, EntityVisualizer<? super T> visualizer, boolean enabledByDefault) {
			this.type = type;
			this.visualizer = visualizer;
			this.enabledByDefault = enabledByDefault;
		}

		public String configKey() {
			return BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
		}

		public boolean defaultEnabled() {
			return enabledByDefault;
		}

		public void set(boolean enabled) {
			if (enabled) {
				VisualizerRegistry.setVisualizer(type, visualizer);
			} else {
				VisualizerRegistry.setVisualizer(type, null);
			}
		}
	}
}
