package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.backend.instancing.entity.IEntityInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.ITileInstanceFactory;
import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;

import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class InstancedRenderRegistry {
	private static final InstancedRenderRegistry INSTANCE = new InstancedRenderRegistry();

	public static InstancedRenderRegistry getInstance() {
		return INSTANCE;
	}

	private final Object2BooleanMap<Object> skipRender = new Object2BooleanLinkedOpenHashMap<>();
	private final Map<TileEntityType<?>, ITileInstanceFactory<?>> tiles = Maps.newHashMap();
	private final Map<EntityType<?>, IEntityInstanceFactory<?>> entities = Maps.newHashMap();

	protected InstancedRenderRegistry() {
		skipRender.defaultReturnValue(false);
	}

	public <T extends TileEntity> boolean shouldSkipRender(T type) {
		return _skipRender(type.getType()) || ((type instanceof IInstanceRendered) && !((IInstanceRendered) type).shouldRenderNormally());
	}

	public <T extends Entity> boolean shouldSkipRender(T type) {
		return _skipRender(type.getType()) || ((type instanceof IInstanceRendered) && !((IInstanceRendered) type).shouldRenderNormally());
	}

	public <T extends TileEntity> boolean canInstance(TileEntityType<? extends T> type) {
		return tiles.containsKey(type);
	}

	public <T extends Entity> boolean canInstance(EntityType<? extends T> type) {
		return entities.containsKey(type);
	}

	public <T extends TileEntity> TileConfig<? extends T> tile(TileEntityType<? extends T> type) {
		return new TileConfig<>(type);
	}

	public <T extends Entity> EntityConfig<? extends T> entity(EntityType<? extends T> type) {
		return new EntityConfig<>(type);
	}

	/**
	 * @deprecated will be removed in 0.3.0, use {@link #tile}
	 */
	@Deprecated
	public <T extends TileEntity> void register(TileEntityType<? extends T> type, ITileInstanceFactory<? super T> rendererFactory) {
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
	public <T extends TileEntity> TileEntityInstance<? super T> create(MaterialManager<?> manager, T tile) {
		TileEntityType<?> type = tile.getType();
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

	public class TileConfig<T extends TileEntity> {

		private final TileEntityType<T> type;

		public TileConfig(TileEntityType<T> type) {
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
