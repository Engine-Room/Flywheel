package com.jozufozu.flywheel.backend;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.ShaderType;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.loading.Shader;
import com.jozufozu.flywheel.backend.loading.ShaderLoadingException;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;

public class ShaderSources {
	public static final String SHADER_DIR = "flywheel/shaders/";
	public static final String PROGRAM_DIR = "flywheel/programs/";
	public static final ArrayList<String> EXTENSIONS = Lists.newArrayList(".vert", ".vsh", ".frag", ".fsh", ".glsl");
	private static final Gson GSON = new GsonBuilder().create();

	private final Map<ResourceLocation, String> shaderSource = new HashMap<>();

	private boolean shouldCrash;
	private final Backend backend;

	public ShaderSources(Backend backend) {
		this.backend = backend;
		ResourceReloadListener.INSTANCE.addCallback(this::onResourceManagerReload);
	}

	public void onResourceManagerReload(IResourceManager manager) {
		backend.refresh();

		if (backend.gl20()) {
			shaderSource.clear();

			shouldCrash = false;

			backend.clearContexts();
			FlywheelEvents.GATHER_CONTEXT.invoker()
					.handleEvent(new GatherContextEvent(backend));

			loadProgramSpecs(manager);
			loadShaderSources(manager);

			for (IShaderContext<?> context : backend.allContexts()) {
				context.load();
			}

			if (shouldCrash) {
				throw new ShaderLoadingException("Could not load all shaders, see log for details");
			}

			Backend.log.info("Loaded all shader programs.");

			// no need to hog all that memory
			shaderSource.clear();

				ClientWorld world = Minecraft.getInstance().level;
				if (Backend.isFlywheelWorld(world)) {
					// TODO: looks like it might be good to have another event here
					InstancedRenderDispatcher.loadAllInWorld(world);
					CrumblingRenderer.reset();
				}
			}
		}
	}

	private void loadProgramSpecs(IResourceManager manager) {
		Collection<ResourceLocation> programSpecs = manager.listResources(PROGRAM_DIR, s -> s.endsWith(".json"));

		for (ResourceLocation location : programSpecs) {
			try {
				IResource file = manager.getResource(location);

				String s = readToString(file.getInputStream());

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

	public void notifyError() {
		shouldCrash = true;
	}

	@NotNull
	public String getShaderSource(ResourceLocation loc) {
		String source = shaderSource.get(loc);

		if (source == null) {
			throw new ShaderLoadingException(String.format("shader '%s' does not exist", loc));
		}

		return source;
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

				String file = readToString(resource.getInputStream());

				ResourceLocation name = ResourceUtil.removePrefixUnchecked(location, SHADER_DIR);

				shaderSource.put(name, file);
			} catch (IOException e) {

			}
		}
	}

	public Shader source(ResourceLocation name, ShaderType type) {
		return new Shader(this, type, name, getShaderSource(name));
	}

	public static Stream<String> lines(String s) {
		return new BufferedReader(new StringReader(s)).lines();
	}

	public String readToString(InputStream is) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		ByteBuffer bytebuffer = null;

		try {
			bytebuffer = readToBuffer(is);
			int i = bytebuffer.position();
			((Buffer) bytebuffer).rewind();
			return MemoryUtil.memASCII(bytebuffer, i);
		} catch (IOException e) {

		} finally {
			if (bytebuffer != null) {
				MemoryUtil.memFree(bytebuffer);
			}

		}

		return null;
	}

	public ByteBuffer readToBuffer(InputStream is) throws IOException {
		ByteBuffer bytebuffer;
		if (is instanceof FileInputStream) {
			FileInputStream fileinputstream = (FileInputStream) is;
			FileChannel filechannel = fileinputstream.getChannel();
			bytebuffer = MemoryUtil.memAlloc((int) filechannel.size() + 1);

			while (filechannel.read(bytebuffer) != -1) {
			}
		} else {
			bytebuffer = MemoryUtil.memAlloc(8192);
			ReadableByteChannel readablebytechannel = Channels.newChannel(is);

			while (readablebytechannel.read(bytebuffer) != -1) {
				if (bytebuffer.remaining() == 0) {
					bytebuffer = MemoryUtil.memRealloc(bytebuffer, bytebuffer.capacity() * 2);
				}
			}
		}

		return bytebuffer;
	}

	public static class ResourceReloadListener extends ReloadListener<Unit> implements IdentifiableResourceReloadListener {
		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		public static final ResourceLocation ID = new ResourceLocation(Flywheel.ID, "shader_sources");
		public static final Collection<ResourceLocation> DEPENDENCIES = Arrays.asList(ResourceReloadListenerKeys.TEXTURES, ResourceReloadListenerKeys.MODELS);

		private final List<Consumer<IResourceManager>> callbacks = new ArrayList<>();

		@Override
		protected Unit prepare(IResourceManager resourceManager, IProfiler profiler) {
			return Unit.INSTANCE;
		}

		@Override
		protected void apply(Unit object, IResourceManager resourceManager, IProfiler profiler) {
			callbacks.forEach(callback -> callback.accept(resourceManager));
		}

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public Collection<ResourceLocation> getFabricDependencies() {
			return DEPENDENCIES;
		}

		protected void addCallback(Consumer<IResourceManager> callback) {
			callbacks.add(callback);
		}
	}
}
