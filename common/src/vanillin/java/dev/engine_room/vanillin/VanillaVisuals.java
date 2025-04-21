package dev.engine_room.vanillin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.vanillin.compose.ComposableEntityVisual;
import dev.engine_room.vanillin.compose.ConfiguredElement;
import dev.engine_room.vanillin.compose.ConfiguredElementImpl;
import dev.engine_room.vanillin.compose.VisualElement;
import dev.engine_room.vanillin.compose.VisualizationPredicate;
import dev.engine_room.vanillin.config.BlockEntityVisualizerBuilder;
import dev.engine_room.vanillin.config.Configurator;
import dev.engine_room.vanillin.config.EntityVisualizerBuilder;
import dev.engine_room.vanillin.elements.ShadowElement;
import dev.engine_room.vanillin.visuals.BellVisual;
import dev.engine_room.vanillin.visuals.BlockDisplayVisual;
import dev.engine_room.vanillin.visuals.ChestVisual;
import dev.engine_room.vanillin.visuals.ItemDisplayVisual;
import dev.engine_room.vanillin.visuals.ItemFrameVisual;
import dev.engine_room.vanillin.visuals.ItemVisual;
import dev.engine_room.vanillin.visuals.MinecartVisual;
import dev.engine_room.vanillin.visuals.ShulkerBoxVisual;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class VanillaVisuals {
	public static final Configurator CONFIGURATOR = new Configurator();

	// Stable visuals are enabled by default always.
	public static final boolean STABLE = true;
	// Experimental visuals are enabled by default in dev.
	public static final boolean EXPERIMENTAL = VanillinXplat.INSTANCE.isDevelopmentEnvironment();

	public static void init() {
		builder(BlockEntityType.CHEST)
				.factory(ChestVisual::new)
				.apply(STABLE);
		builder(BlockEntityType.ENDER_CHEST)
				.factory(ChestVisual::new)
				.apply(STABLE);
		builder(BlockEntityType.TRAPPED_CHEST)
				.factory(ChestVisual::new)
				.apply(STABLE);

		builder(BlockEntityType.BELL)
				.factory(BellVisual::new)
				.apply(STABLE);

		builder(BlockEntityType.SHULKER_BOX)
				.factory(ShulkerBoxVisual::new)
				.apply(STABLE);

		builder(EntityType.BLOCK_DISPLAY).factory(BlockDisplayVisual::new)
				.apply(STABLE);

		composable(EntityType.ITEM_DISPLAY).with(element(VisualElements.ITEM_DISPLAY).build())
				.shouldVisualize((ctx, e) -> ItemDisplayVisual.shouldVisualize(e))
				.build()
				.skipVanillaRender(ItemDisplayVisual::shouldVisualize)
				.apply(EXPERIMENTAL);

		minecart(EntityType.CHEST_MINECART, ModelLayers.CHEST_MINECART)
				.apply(STABLE);
		minecart(EntityType.COMMAND_BLOCK_MINECART, ModelLayers.COMMAND_BLOCK_MINECART)
				.apply(STABLE);
		minecart(EntityType.FURNACE_MINECART, ModelLayers.FURNACE_MINECART)
				.apply(STABLE);
		minecart(EntityType.HOPPER_MINECART, ModelLayers.HOPPER_MINECART)
				.apply(STABLE);
		minecart(EntityType.MINECART, ModelLayers.MINECART)
				.apply(STABLE);
		minecart(EntityType.SPAWNER_MINECART, ModelLayers.SPAWNER_MINECART)
				.apply(STABLE);

		composable(EntityType.TNT_MINECART).apply(VanillaVisuals::commonElements)
				.with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.7f, ShadowElement.Config.DEFAULT_STRENGTH))
						.build())
				.with(element(VisualElements.FIRE).build())
				.with(element(VisualElements.TNT_MINECART).build())
				.build()
				.skipVanillaRender(MinecartVisual::shouldSkipRender)
				.apply(STABLE);

		itemFrame(EntityType.ITEM_FRAME).apply(EXPERIMENTAL);
		itemFrame(EntityType.GLOW_ITEM_FRAME).apply(EXPERIMENTAL);

		composable(EntityType.ITEM).apply(VanillaVisuals::commonElements)
				.with(element(VisualElements.FIRE).build())
				.with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.15f, 0.75f))
						.build())
				.with(element(VisualElements.ITEM_ENTITY).build())
				.shouldVisualize(((ctx, entity) -> ItemVisual.isSupported(entity)))
				.build()
				.skipVanillaRender(ItemVisual::isSupported)
				.apply(EXPERIMENTAL);

	}

	public static <T extends Entity> void commonElements(EntityBuilder<T> builder) {
		builder.with(element(VisualElements.HITBOX).configure(false)
				.build());
	}

	public static <T extends ItemFrame> EntityVisualizerBuilder<T> itemFrame(EntityType<T> type) {
		return composable(type).apply(VanillaVisuals::commonElements)
				.with(element(VisualElements.ITEM_FRAME).build())
				.shouldVisualize((ctx, entity) -> ItemFrameVisual.shouldVisualize(entity))
				.build()
				.skipVanillaRender(ItemFrameVisual::shouldVisualize);
	}

	public static <T extends AbstractMinecart> EntityVisualizerBuilder<T> minecart(EntityType<T> type, ModelLayerLocation variant) {
		return composable(type).apply(VanillaVisuals::commonElements)
				.with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.7f, ShadowElement.Config.DEFAULT_STRENGTH))
						.build())
				.with(element(VisualElements.FIRE).build())
				.with(element(VisualElements.MINECART).configure(variant)
						.build())
				.build()
				.skipVanillaRender(MinecartVisual::shouldSkipRender);
	}

	public static <T extends Entity> EntityBuilder<T> composable(EntityType<T> entityType) {
		return new EntityBuilder<>(entityType);
	}

	public static <T, C> ConfiguredElementImpl.ConfiguredElementBuilder<T, C> element(VisualElement<T, C> element) {
		return new ConfiguredElementImpl.ConfiguredElementBuilder<>(element);
	}

	public static class EntityBuilder<T extends Entity> {
		private final List<ConfiguredElement<? super T>> elements = new ArrayList<>();

		private final EntityType<T> entityType;
		@Nullable
		private VisualizationPredicate<T> predicate;

		public EntityBuilder(EntityType<T> entityType) {
			this.entityType = entityType;
		}

		/**
		 * Set a predicate to control whether <em>all</em> elements are visualized.
		 * <p>This is useful when you can't guarantee than an entity will support visualization for its entire lifetime.
		 *
		 * @param predicate A visualization predicate, returning {@code true} to indicate the entity should be visualized.
		 */
		public EntityBuilder<T> shouldVisualize(VisualizationPredicate<T> predicate) {
			this.predicate = predicate;
			return this;
		}

		/**
		 * Add a configured visual element to this visualizer.
		 *
		 * @param element The configured visual element.
		 */
		public EntityBuilder<T> with(ConfiguredElement<? super T> element) {
			elements.add(element);
			return this;
		}

		public EntityBuilder<T> apply(Consumer<EntityBuilder<T>> mutate) {
			mutate.accept(this);
			return this;
		}

		public EntityVisualizerBuilder<T> build() {
			var elementsArray = elements.toArray(new ConfiguredElement[0]);

			if (predicate == null) {
				predicate = VisualizationPredicate.alwaysTrue();
			}

			var controller = new ComposableEntityVisual.Controller<T>(elementsArray, predicate);

			return builder(entityType).factory((ctx, entity, partialTick) -> new ComposableEntityVisual<T>(ctx, entity, partialTick, controller));
		}
	}

	public static <T extends BlockEntity> BlockEntityVisualizerBuilder<T> builder(BlockEntityType<T> type) {
		return new BlockEntityVisualizerBuilder<>(CONFIGURATOR, type);
	}

	public static <T extends Entity> EntityVisualizerBuilder<T> builder(EntityType<T> type) {
		return new EntityVisualizerBuilder<>(CONFIGURATOR, type);
	}
}
