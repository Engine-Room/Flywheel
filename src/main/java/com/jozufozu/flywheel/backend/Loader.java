package com.jozufozu.flywheel.backend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.GameStateRegistry;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.shader.ProgramSpec;
import com.jozufozu.flywheel.core.source.Resolver;
import com.jozufozu.flywheel.core.source.ShaderSources;
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
	public static final String PROGRAM_DIR = "flywheel/programs";
	public static final String PROGRAM_DIR_SLASH = PROGRAM_DIR + "/";
	private static final Gson GSON = new GsonBuilder().create();

	private final Map<ResourceLocation, ProgramSpec> programs = new HashMap<>();

	private boolean firstLoad = true;

	Loader() {
		ResourceReloadListener.INSTANCE.addCallback(this::onResourceManagerReload);
	}

	@Nullable
	public ProgramSpec get(ResourceLocation name) {
		return programs.get(name);
	}

	public void onResourceManagerReload(ResourceManager manager) {
		Backend.refresh();

		GameStateRegistry._clear();

		Resolver.INSTANCE.invalidate();
		FlywheelEvents.GATHER_CONTEXT.invoker()
				.handleEvent(new GatherContextEvent(firstLoad));

		ShaderSources sources = new ShaderSources(manager);

		loadProgramSpecs(manager);

		Resolver.INSTANCE.run(sources);

		Backend.LOGGER.info("Loaded all shader sources.");

		ClientLevel world = Minecraft.getInstance().level;
		if (Backend.canUseInstancing(world)) {
			// TODO: looks like it might be good to have another event here
			InstancedRenderDispatcher.resetInstanceWorld(world);
			CrumblingRenderer.reset();
		}

		firstLoad = false;
	}

	private void loadProgramSpecs(ResourceManager manager) {
		programs.clear();

		Map<ResourceLocation, Resource> programSpecs = manager.listResources(PROGRAM_DIR, loc -> loc.getPath().endsWith(".json"));

		programSpecs.forEach((location, resource) -> {
			try (InputStream inputStream = resource.open()) {
				String s = StringUtil.readToString(inputStream);

				ResourceLocation specName = ResourceUtil.trim(location, PROGRAM_DIR_SLASH, ".json");

				DataResult<Pair<ProgramSpec, JsonElement>> result = ProgramSpec.CODEC.decode(JsonOps.INSTANCE, GSON.fromJson(s, JsonElement.class));

				ProgramSpec spec = result.get()
						.orThrow()
						.getFirst();

				spec.setName(specName);

				if (programs.containsKey(specName)) {
					throw new IllegalStateException("Program spec '" + specName + "' already registered.");
				}
				programs.put(specName, spec);

			} catch (Exception e) {
				Backend.LOGGER.error("Could not load program " + location, e);
			}
		});
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
