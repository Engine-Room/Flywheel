package com.jozufozu.flywheel.backend;

import java.util.Collection;
import java.util.function.Predicate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.source.Resolver;
import com.jozufozu.flywheel.backend.source.ShaderLoadingException;
import com.jozufozu.flywheel.backend.source.ShaderSources;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.util.ResourceUtil;
import com.jozufozu.flywheel.util.StreamUtil;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

/**
 * The main entity for loading shaders.
 *
 * <p>
 *     This class is responsible for invoking the loading, parsing, and compilation stages.
 * </p>
 */
public class Loader implements ISelectiveResourceReloadListener {
	public static final String PROGRAM_DIR = "flywheel/programs/";
	private static final Gson GSON = new GsonBuilder().create();

	private final Backend backend;
	private boolean shouldCrash;

	private boolean firstLoad = true;

	public Loader(Backend backend) {
		this.backend = backend;

		// Can be null when running datagenerators due to the unfortunate time we call this
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft != null) {
			IResourceManager manager = minecraft.getResourceManager();
			if (manager instanceof IReloadableResourceManager) {
				((IReloadableResourceManager) manager).registerReloadListener(this);
			}
		}
	}

	public void notifyError() {
		shouldCrash = true;
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate) {
		if (predicate.test(VanillaResourceType.SHADERS)) {
			backend.refresh();

			if (backend.gl20()) {
				shouldCrash = false;
				backend._clearContexts();

				Resolver.INSTANCE.invalidate();
				ModLoader.get()
						.postEvent(new GatherContextEvent(backend, firstLoad));

				ShaderSources sources = new ShaderSources(manager);

				loadProgramSpecs(manager);

				Resolver.INSTANCE.resolve(sources);

				for (IShaderContext<?> context : backend.allContexts()) {
					context.load();
				}

				if (shouldCrash) {
					throw new ShaderLoadingException("Could not load all shaders, see log for details");
				}

				Backend.log.info("Loaded all shader programs.");

				ClientWorld world = Minecraft.getInstance().level;
				if (Backend.isFlywheelWorld(world)) {
					// TODO: looks like it might be good to have another event here
					InstancedRenderDispatcher.loadAllInWorld(world);
					CrumblingRenderer.reset();
				}
			}

			firstLoad = false;
		}
	}

	private void loadProgramSpecs(IResourceManager manager) {
		Collection<ResourceLocation> programSpecs = manager.listResources(PROGRAM_DIR, s -> s.endsWith(".json"));

		for (ResourceLocation location : programSpecs) {
			try {
				IResource file = manager.getResource(location);

				String s = StreamUtil.readToString(file.getInputStream());

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
}
