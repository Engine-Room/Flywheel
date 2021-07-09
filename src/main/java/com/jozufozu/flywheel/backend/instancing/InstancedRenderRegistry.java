package com.jozufozu.flywheel.backend.instancing;

import java.util.Map;

import javax.annotation.Nullable;

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

	public <T extends TileEntity> TileRegistrater<? extends T> tile(TileEntityType<? extends T> type) {
		return new TileRegistrater<>(type);
	}

	public <T extends Entity> EntityRegistrater<? extends T> entity(EntityType<? extends T> type) {
		return new EntityRegistrater<>(type);
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

	public class TileRegistrater<T extends TileEntity> {

		private final TileEntityType<T> type;
		private ITileInstanceFactory<? super T> factory;
		private boolean skipRender = false;

		public TileRegistrater(TileEntityType<T> type) {
			this.type = type;
		}

		public TileRegistrater<T> factory(ITileInstanceFactory<? super T> rendererFactory) {
			factory = rendererFactory;
			return this;
		}

		public TileRegistrater<T> setSkipRender(boolean skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		public InstancedRenderRegistry build() {
			tiles.put(type, factory);
			InstancedRenderRegistry.this.skipRender.put(type, skipRender);

			return InstancedRenderRegistry.this;
		}
	}

	public class EntityRegistrater<T extends Entity> {

		private final EntityType<T> type;
		private IEntityInstanceFactory<? super T> factory;
		private boolean skipRender = false;

		public EntityRegistrater(EntityType<T> type) {
			this.type = type;
		}

		public EntityRegistrater<T> factory(IEntityInstanceFactory<? super T> rendererFactory) {
			factory = rendererFactory;
			return this;
		}

		public EntityRegistrater<T> setSkipRender(boolean skipRender) {
			this.skipRender = skipRender;
			return this;
		}

		public InstancedRenderRegistry build() {
			entities.put(type, factory);
			InstancedRenderRegistry.this.skipRender.put(type, skipRender);

			return InstancedRenderRegistry.this;
		}
	}

}
