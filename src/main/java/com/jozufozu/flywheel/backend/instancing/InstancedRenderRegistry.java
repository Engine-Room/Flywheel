package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.instancing.entity.IEntityInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.ITileInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.BlockEntityInstance;
import com.jozufozu.flywheel.backend.material.MaterialManager;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;

public class InstancedRenderRegistry {
	private static final InstancedRenderRegistry INSTANCE = new InstancedRenderRegistry();

	public static InstancedRenderRegistry getInstance() {
		return INSTANCE;
	}

	private final Object2BooleanMap<Object> skipRender = new Object2BooleanLinkedOpenHashMap<>();
	private final Map<BlockEntityType<?>, ITileInstanceFactory<?>> tiles = Maps.newHashMap();
	private final Map<EntityType<?>, IEntityInstanceFactory<?>> entities = Maps.newHashMap();

	protected InstancedRenderRegistry() {
		skipRender.defaultReturnValue(false);
	}

	public <T extends BlockEntity> boolean shouldSkipRender(T type) {
		return _skipRender(type.getType()) || ((type instanceof IInstanceRendered) && !((IInstanceRendered) type).shouldRenderNormally());
	}

	public <T extends Entity> boolean shouldSkipRender(T type) {
		return _skipRender(type.getType()) || ((type instanceof IInstanceRendered) && !((IInstanceRendered) type).shouldRenderNormally());
	}

	public <T extends BlockEntity> boolean canInstance(BlockEntityType<? extends T> type) {
		return tiles.containsKey(type);
	}

	public <T extends Entity> boolean canInstance(EntityType<? extends T> type) {
		return entities.containsKey(type);
	}

	public <T extends BlockEntity> TileConfig<? extends T> tile(BlockEntityType<? extends T> type) {
		return new TileConfig<>(type);
	}

	public <T extends Entity> EntityConfig<? extends T> entity(EntityType<? extends T> type) {
		return new EntityConfig<>(type);
	}

	/**
	 * @deprecated will be removed in 0.3.0, use {@link #tile}
	 */
	@Deprecated
	public <T extends BlockEntity> void register(BlockEntityType<? extends T> type, ITileInstanceFactory<? super T> rendererFactory) {
		this.tile(type)
				.factory(rendererFactory);
	}

	/**
	 * @deprecated will be removed in 0.3.0, use {@link #entity}
	 */
	@Deprecated
	public <T extends Entity> void register(EntityType<? extends T> type, IEntityInstanceFactory<? super T> rendererFactory) {
		this.entity(type)
				.factory(rendererFactory);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends BlockEntity> BlockEntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		BlockEntityType<?> type = tile.getType();
		ITileInstanceFactory<? super T> factory = (ITileInstanceFactory<? super T>) this.tiles.get(type);

		if (factory == null) return null;
		else return factory.create(manager, tile);
	}


	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Entity> EntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		EntityType<?> type = tile.getType();
		IEntityInstanceFactory<? super T> factory = (IEntityInstanceFactory<? super T>) this.entities.get(type);

		if (factory == null) return null;
		else return factory.create(manager, tile);
	}

	private boolean _skipRender(Object o) {
		return skipRender.getBoolean(o);
	}

	public class TileConfig<T extends BlockEntity> {

		private final BlockEntityType<T> type;

		public TileConfig(BlockEntityType<T> type) {
			this.type = type;
		}

		public TileConfig<T> factory(ITileInstanceFactory<? super T> rendererFactory) {
			tiles.put(type, rendererFactory);
			return this;
		}

		public TileConfig<T> setSkipRender(boolean skipRender) {
			InstancedRenderRegistry.this.skipRender.put(type, skipRender);
			return this;
		}
	}

	public class EntityConfig<T extends Entity> {

		private final EntityType<T> type;

		public EntityConfig(EntityType<T> type) {
			this.type = type;
		}

		public EntityConfig<T> factory(IEntityInstanceFactory<? super T> rendererFactory) {
			entities.put(type, rendererFactory);
			return this;
		}

		public EntityConfig<T> setSkipRender(boolean skipRender) {
			InstancedRenderRegistry.this.skipRender.put(type, skipRender);

			return this;
		}
	}

}
