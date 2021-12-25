package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.source.Resolver;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.backend.source.ShaderSources;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.jozufozu.flywheel.util.ResourceUtil;
import com.jozufozu.flywheel.util.StringUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * The main entity for loading shaders.
 *
 * <p>
 * This class is responsible for invoking the loading, parsing, and compilation stages.
 * </p>
 */
public class Loader {
	public static final String PROGRAM_DIR = "flywheel/programs/";
	private static final Gson GSON = new GsonBuilder().create();

	private final Backend backend;
	private boolean shouldCrash;

	private boolean firstLoad = true;

	public Loader(Backend backend) {
		this.backend = backend;

		ResourceReloadListener.INSTANCE.addCallback(this::onResourceManagerReload);
	}

	public void notifyError() {
		shouldCrash = true;
	}

	public void onResourceManagerReload(ResourceManager manager) {
		backend.refresh();

		shouldCrash = false;
		backend._clearContexts();

		Resolver.INSTANCE.invalidate();
		FlywheelEvents.GATHER_CONTEXT.invoker()
				.handleEvent(new GatherContextEvent(backend, firstLoad));

		ShaderSources sources = new ShaderSources(manager);

		loadProgramSpecs(manager);

		Resolver.INSTANCE.resolve(sources);

		for (ShaderContext<?> context : backend.allContexts()) {
			context.load();
		}

		if (shouldCrash) {
			throw new ShaderLoadingException("Could not load all shaders, see log for details");
		}

		Backend.log.info("Loaded all shader programs.");

		ClientLevel world = Minecraft.getInstance().level;
		if (Backend.isFlywheelWorld(world)) {
			// TODO: looks like it might be good to have another event here
			InstancedRenderDispatcher.resetInstanceWorld(world);
			CrumblingRenderer.reset();
		}

		firstLoad = false;
	}

	private void loadProgramSpecs(ResourceManager manager) {
		Collection<ResourceLocation> programSpecs = manager.listResources(PROGRAM_DIR, s -> s.endsWith(".json"));

		for (ResourceLocation location : programSpecs) {
			try {
				Resource file = manager.getResource(location);

				String s = StringUtil.readToString(file.getInputStream());

				ResourceLocation specName = ResourceUtil.trim(location, PROGRAM_DIR, ".json");

				DataResult<Pair<ProgramSpec, JsonElement>> result = ProgramSpec.CODEC.decode(JsonOps.INSTANCE, GSON.fromJson(s, JsonElement.class));

				ProgramSpec spec = result.get()
						.orThrow()
						.getFirst();

				spec.setName(specName);

				backend.register(spec);
			} catch (Exception e) {
				Backend.log.error(e);
			}
		}
	}

	public static class ResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		public static final ResourceLocation ID = Flywheel.rl("loaders");
		public static final List<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.TEXTURES, ResourceReloadListenerKeys.MODELS);

		private final List<Consumer<ResourceManager>> callbacks = new ArrayList<>();

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			callbacks.forEach(callback -> callback.accept(resourceManager));
		}

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public List<ResourceLocation> getFabricDependencies() {
			return DEPENDENCIES;
		}

		protected void addCallback(Consumer<ResourceManager> callback) {
			callbacks.add(callback);
		}
	}
}
