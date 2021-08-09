package com.jozufozu.flywheel.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.backend.pipeline.SourceFile;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.event.GatherContextEvent;
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

public class ShaderSources implements ISelectiveResourceReloadListener {
	public static final String SHADER_DIR = "flywheel/shaders/";
	public static final String PROGRAM_DIR = "flywheel/programs/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");
	private static final Gson GSON = new GsonBuilder().create();

	private final Map<ResourceLocation, SourceFile> shaderSources = new HashMap<>();

	private final Map<ResourceLocation, FileResolution> resolutions = new HashMap<>();

	private boolean shouldCrash;
	private final Backend backend;

	public Index index;

	public ShaderSources(Backend backend) {
		this.backend = backend;
		IResourceManager manager = backend.minecraft.getResourceManager();
		if (manager instanceof IReloadableResourceManager) {
			((IReloadableResourceManager) manager).registerReloadListener(this);
		}
	}

	public SourceFile source(ResourceLocation name) {
		SourceFile source = shaderSources.get(name);

		if (source == null) {
			throw new ShaderLoadingException(String.format("shader '%s' does not exist", name));
		}

		return source;
	}

	public FileResolution resolveFile(ResourceLocation fileLoc) {
		return resolutions.computeIfAbsent(fileLoc, FileResolution::new);
	}

	@Deprecated
	public void notifyError() {
		shouldCrash = true;
	}

	@Override
	public void onResourceManagerReload(IResourceManager manager, Predicate<IResourceType> predicate) {
		if (predicate.test(VanillaResourceType.SHADERS)) {
			backend.refresh();

			if (backend.gl20()) {
				shouldCrash = false;

				backend.clearContexts();
				ModLoader.get()
						.postEvent(new GatherContextEvent(backend));

				resolutions.values().forEach(FileResolution::invalidate);

				loadProgramSpecs(manager);
				loadShaderSources(manager);

				for (FileResolution resolution : resolutions.values()) {
					resolution.resolve(this);
				}

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
		}
	}

	private void loadShaderSources(IResourceManager manager) {
		Collection<ResourceLocation> allShaders = manager.listResources(SHADER_DIR, s -> {
			for (String ext : EXTENSIONS) {
				if (s.endsWith(ext)) return true;
			}
			return false;
		});

		for (ResourceLocation location : allShaders) {
			try {
				IResource resource = manager.getResource(location);

				String source = StreamUtil.readToString(resource.getInputStream());

				ResourceLocation name = ResourceUtil.removePrefixUnchecked(location, SHADER_DIR);

				shaderSources.put(name, new SourceFile(this, name, source));
			} catch (IOException e) {

			}
		}

		index = new Index(shaderSources);
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
