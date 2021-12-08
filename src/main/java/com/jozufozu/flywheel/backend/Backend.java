package com.jozufozu.flywheel.backend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Backend {
	public static final Logger log = LogManager.getLogger(Backend.class);

	protected static final Backend INSTANCE = new Backend();

	public static Backend getInstance() {
		return INSTANCE;
	}

	public final Loader loader;

	public GLCapabilities capabilities;
	public GlCompat compat;

	private boolean instancedArrays;
	private boolean enabled;

	private final List<IShaderContext<?>> contexts = new ArrayList<>();
	private final Map<ResourceLocation, MaterialSpec<?>> materialRegistry = new HashMap<>();
	private final Map<ResourceLocation, ProgramSpec> programSpecRegistry = new HashMap<>();

	protected Backend() {
		loader = new Loader(this);

		OptifineHandler.init();
	}

	/**
	 * Get a string describing the Flywheel backend. When there are eventually multiple backends
	 * (Meshlet, MDI, GL31 Draw Instanced are planned), this will name which one is in use.
	 */
	public String getBackendDescriptor() {
		if (canUseInstancing()) {
			return "GL33 Instanced Arrays";
		}

		if (canUseVBOs()) {
			return "VBOs";
		}

		return "Disabled";
	}

	/**
	 * Register a shader program.
	 */
	public ProgramSpec register(ProgramSpec spec) {
		ResourceLocation name = spec.name;
		if (programSpecRegistry.containsKey(name)) {
			throw new IllegalStateException("Program spec '" + name + "' already registered.");
		}
		programSpecRegistry.put(name, spec);
		return spec;
	}

	/**
	 * Register a shader context.
	 */
	public <C extends IShaderContext<?>> C register(C spec) {
		contexts.add(spec);
		return spec;
	}

	/**
	 * Register an instancing material.
	 */
	public <D extends InstanceData> MaterialSpec<D> register(MaterialSpec<D> spec) {
		ResourceLocation name = spec.name;
		if (materialRegistry.containsKey(name)) {
			throw new IllegalStateException("Material spec '" + name + "' already registered.");
		}
		materialRegistry.put(name, spec);

		log.debug("registered material '" + name + "' with vertex size " + spec.getModelFormat()
				.getStride() + " and instance size " + spec.getInstanceType().format().getStride());

		return spec;
	}

	public ProgramSpec getSpec(ResourceLocation name) {
		return programSpecRegistry.get(name);
	}

	public boolean available() {
		return canUseVBOs();
	}

	public boolean canUseInstancing() {
		return enabled && instancedArrays;
	}

	public boolean canUseVBOs() {
		return enabled && gl20();
	}

	public boolean gl33() {
		return capabilities.OpenGL33;
	}

	public boolean gl20() {
		return capabilities.OpenGL20;
	}

	public void refresh() {
		OptifineHandler.refresh();
		capabilities = GL.createCapabilities();

		compat = new GlCompat(capabilities);

		instancedArrays = compat.instancedArraysSupported();

		enabled = FlwConfig.get()
				.enabled() && !OptifineHandler.usingShaders();
	}

	public boolean canUseInstancing(@Nullable Level world) {
		return canUseInstancing() && isFlywheelWorld(world);
	}

	public Collection<MaterialSpec<?>> allMaterials() {
		return materialRegistry.values();
	}

	public Collection<ProgramSpec> allPrograms() {
		return programSpecRegistry.values();
	}

	public Collection<IShaderContext<?>> allContexts() {
		return contexts;
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) worlds that don't specifically support it.
	 */
	public static boolean isFlywheelWorld(@Nullable LevelAccessor world) {
		if (world == null) return false;

		if (!world.isClientSide()) return false;

		if (world instanceof IFlywheelWorld && ((IFlywheelWorld) world).supportsFlywheel()) return true;

		return world == Minecraft.getInstance().level;
	}

	public static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

	public static void reloadWorldRenderers() {
		RenderWork.enqueue(Minecraft.getInstance().levelRenderer::allChanged);
	}

	/**
	 * INTERNAL USE ONLY
	 */
	public void _clearContexts() {
		GameStateRegistry.clear();
		programSpecRegistry.clear();
		contexts.forEach(IShaderContext::delete);
		contexts.clear();
		materialRegistry.clear();
	}

	public static void init() {
	}
}
