package com.jozufozu.flywheel.backend.instancing;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE4;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.util.BitSet;
import java.util.SortedSet;
import java.util.Vector;

import javax.annotation.Nonnull;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstanceManager;
import com.jozufozu.flywheel.backend.instancing.tile.TileInstanceManager;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.CrumblingInstanceManager;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.WorldAttached;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class InstancedRenderDispatcher {

	private static final WorldAttached<EntityInstanceManager> entityInstanceManager = new WorldAttached<>(world -> new EntityInstanceManager(Contexts.WORLD.getMaterialManager(world)));
	private static final WorldAttached<TileInstanceManager> tileInstanceManager = new WorldAttached<>(world -> new TileInstanceManager(Contexts.WORLD.getMaterialManager(world)));

	private static final LazyValue<Vector<CrumblingInstanceManager>> blockBreaking = new LazyValue<>(() -> {
		Vector<CrumblingInstanceManager> renderers = new Vector<>(10);
		for (int i = 0; i < 10; i++) {
			renderers.add(new CrumblingInstanceManager());
		}
		return renderers;
	});

	@Nonnull
	public static TileInstanceManager getTiles(IWorld world) {
		return tileInstanceManager.get(world);
	}

	@Nonnull
	public static EntityInstanceManager getEntities(IWorld world) {
		return entityInstanceManager.get(world);
	}

	private static final RenderType crumblingLayer = ModelBakery.DESTROY_TYPES.get(0);

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {

		if (!Backend.isGameActive() || event.phase == TickEvent.Phase.START) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		ClientWorld world = mc.level;
		AnimationTickHolder.tick();

		Entity renderViewEntity = mc.cameraEntity != null ? mc.cameraEntity : mc.player;

		if (renderViewEntity == null) return;

		getTiles(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
		getEntities(world).tick(renderViewEntity.getX(), renderViewEntity.getY(), renderViewEntity.getZ());
	}

	public static void enqueueUpdate(TileEntity te) {
		getTiles(te.getLevel()).queueUpdate(te);
	}

	@SubscribeEvent
	public static void onBeginFrame(BeginFrameEvent event) {
		Contexts.WORLD.getMaterialManager(event.getWorld())
				.checkAndShiftOrigin(event.getInfo());

		getTiles(event.getWorld()).beginFrame(event.getInfo());
		getEntities(event.getWorld()).beginFrame(event.getInfo());
	}

	public static void enqueueUpdate(Entity entity) {
		getEntities(entity.level).queueUpdate(entity);
	}

	@SubscribeEvent
	public static void renderLayer(RenderLayerEvent event) {
		ClientWorld world = event.getWorld();
		if (!Backend.getInstance().canUseInstancing(world)) return;

		event.type.setupRenderState();

		Contexts.WORLD.getMaterialManager(world)
				.render(event.type, event.viewProjection, event.camX, event.camY, event.camZ);

		event.type.clearRenderState();
	}

	@SubscribeEvent
	public static void onReloadRenderers(ReloadRenderersEvent event) {
		ClientWorld world = event.getWorld();
		if (Backend.getInstance().canUseInstancing() && world != null) {
			Contexts.WORLD.getMaterialManager(world).delete();

			TileInstanceManager tiles = getTiles(world);
			tiles.invalidate();
			world.blockEntityList.forEach(tiles::add);

			EntityInstanceManager entities = getEntities(world);
			entities.invalidate();
			world.entitiesForRendering().forEach(entities::add);
		}
	}

	public static void renderBreaking(ClientWorld world, Matrix4f viewProjection, double cameraX, double cameraY, double cameraZ) {
		if (!Backend.getInstance().canUseInstancing(world)) return;

		WorldRenderer worldRenderer = Minecraft.getInstance().levelRenderer;
		Long2ObjectMap<SortedSet<DestroyBlockProgress>> breakingProgressions = worldRenderer.destructionProgress;

		if (breakingProgressions.isEmpty()) return;
		Vector<CrumblingInstanceManager> renderers = blockBreaking.get();

		BitSet bitSet = new BitSet(10);

		for (Long2ObjectMap.Entry<SortedSet<DestroyBlockProgress>> entry : breakingProgressions.long2ObjectEntrySet()) {
			BlockPos breakingPos = BlockPos.of(entry.getLongKey());

			SortedSet<DestroyBlockProgress> progresses = entry.getValue();
			if (progresses != null && !progresses.isEmpty()) {
				int blockDamage = progresses.last().getProgress();
				bitSet.set(blockDamage);
				renderers.get(blockDamage).add(world.getBlockEntity(breakingPos));
			}
		}

		TextureManager textureManager = Minecraft.getInstance().textureManager;
		ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getMainCamera();

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, textureManager.getTexture(PlayerContainer.BLOCK_ATLAS).getId());

		glActiveTexture(GL_TEXTURE4);

		crumblingLayer.setupRenderState();
		bitSet.stream().forEach(i -> {
			Texture breaking = textureManager.getTexture(ModelBakery.BREAKING_LOCATIONS.get(i));
			CrumblingInstanceManager renderer = renderers.get(i);
			renderer.beginFrame(info);

			if (breaking != null) {
				glBindTexture(GL_TEXTURE_2D, breaking.getId());
				renderer.materialManager.render(RenderType.cutoutMipped(), viewProjection, cameraX, cameraY, cameraZ);
			}

			renderer.invalidate();
		});
		crumblingLayer.clearRenderState();

		glActiveTexture(GL_TEXTURE0);
		Texture breaking = textureManager.getTexture(ModelBakery.BREAKING_LOCATIONS.get(0));
		if (breaking != null)
			glBindTexture(GL_TEXTURE_2D, breaking.getId());
	}
}
