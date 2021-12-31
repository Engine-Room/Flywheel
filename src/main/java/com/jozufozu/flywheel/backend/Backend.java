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

import com.jozufozu.flywheel.api.FlywheelWorld;
import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwEngine;
import com.jozufozu.flywheel.core.shader.spec.ProgramSpec;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class Backend {
	public static final Logger LOGGER = LogManager.getLogger(Backend.class);

	protected static final Backend INSTANCE = new Backend();
	public static Backend getInstance() {
		return INSTANCE;
	}

	private FlwEngine engine;

	public GLCapabilities capabilities;
	public GlCompat compat;

	public final Loader loader;
	private final List<ShaderContext<?>> contexts = new ArrayList<>();
	private final Map<ResourceLocation, StructType<?>> materialRegistry = new HashMap<>();
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
		return engine.getProperName();
	}

	public FlwEngine getEngine() {
		return engine;
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
	public <C extends ShaderContext<?>> C register(C spec) {
		contexts.add(spec);
		return spec;
	}

	/**
	 * Register an instancing material.
	 */
	public <D extends InstanceData> StructType<D> register(ResourceLocation name, StructType<D> spec) {
		if (materialRegistry.containsKey(name)) {
			throw new IllegalStateException("Material spec '" + name + "' already registered.");
		}
		materialRegistry.put(name, spec);

		LOGGER.debug("registered material '" + name + "' with instance size " + spec.getLayout().getStride());

		return spec;
	}

	public ProgramSpec getSpec(ResourceLocation name) {
		return programSpecRegistry.get(name);
	}

	public void refresh() {
		OptifineHandler.refresh();

		capabilities = GL.createCapabilities();

		compat = new GlCompat(capabilities);

		engine = chooseEngine(compat);
	}

	public Collection<StructType<?>> allMaterials() {
		return materialRegistry.values();
	}

	public Collection<ProgramSpec> allPrograms() {
		return programSpecRegistry.values();
	}

	public Collection<ShaderContext<?>> allContexts() {
		return contexts;
	}

	public static boolean isOn() {
		return getInstance().engine != FlwEngine.OFF;
	}

	public static boolean canUseInstancing(@Nullable Level world) {
		return isOn() && isFlywheelWorld(world);
	}

	/**
	 * Used to avoid calling Flywheel functions on (fake) worlds that don't specifically support it.
	 */
	public static boolean isFlywheelWorld(@Nullable LevelAccessor world) {
		if (world == null) return false;

		if (!world.isClientSide()) return false;

		if (world instanceof FlywheelWorld && ((FlywheelWorld) world).supportsFlywheel()) return true;

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
	void _clearContexts() {
		GameStateRegistry.clear();
		programSpecRegistry.clear();
		contexts.forEach(ShaderContext::delete);
		contexts.clear();
		materialRegistry.clear();
	}

	public static void init() {
	}

	private static FlwEngine chooseEngine(GlCompat compat) {
		FlwEngine preferredChoice = FlwConfig.get()
				.getEngine();

		boolean usingShaders = OptifineHandler.usingShaders();
		boolean canUseEngine = switch (preferredChoice) {
			case OFF -> true;
			case BATCHING -> !usingShaders;
			case INSTANCING -> !usingShaders && compat.instancedArraysSupported();
		};

		return canUseEngine ? preferredChoice : FlwEngine.OFF;
	}
}
