package dev.engine_room.flywheel.impl;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.backend.engine.AbstractInstancer;
import dev.engine_room.flywheel.backend.engine.DrawManager;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import dev.engine_room.flywheel.lib.memory.FlwMemoryTracker;
import dev.engine_room.flywheel.lib.util.StringUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public final class FlwDebugInfo {

	private FlwDebugInfo() {
	}

	/**
	 * Append a debug data point to the given StringBuilder, formatted as a Markdown list item.
	 */
	public static StringBuilder appendLine(StringBuilder dst, String str) {
		dst.append("\n- ");
		dst.append(str);
		return dst;
	}

	/**
	 * Append a header to the given StringBuilder.
	 */
	public static void appendHeader(StringBuilder dst, String str) {
		dst.append("\n## ");
		dst.append(str);
	}

	public static void appendHeader2(StringBuilder dst, String str) {
		dst.append("\n### ");
		dst.append(str);
	}

	public static Component getDebugCommandInfo() {
		StringBuilder out = new StringBuilder("# `/flywheel debug info`");

		VisualizationManagerImpl manager = VisualizationManagerImpl.get(Minecraft.getInstance().level);

		addImplDebugInfo(out);
		addSystemDebugInfo(out);
		addOpenGLDebugInfo(out);
		addBackendDebugInfo(manager, out);
		addVisualizationManagerDebugInfo(manager, out);

		// Write out to a string both to emit to chat and include in the click event.
		var debugInfoString = out.toString();

		return Component.literal(debugInfoString)
				.append(Component.literal("\n\nClick to copy debug info to clipboard")
						.withStyle(Style.EMPTY.withUnderlined(true)
								.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, debugInfoString))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(debugInfoString)))))
				.append(Component.literal("\n\nClick to open an issue on GitHub")
						.withStyle(Style.EMPTY.withUnderlined(true)
								.withColor(ChatFormatting.BLUE)
								.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Engine-Room/Flywheel/issues"))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Opens URL:\nhttps://github.com/Engine-Room/Flywheel/issues")))));

	}

	private static void addBackendDebugInfo(@Nullable VisualizationManagerImpl manager, StringBuilder out) {
		appendHeader(out, "Backend");

		if (manager == null) {
			appendLine(out, "No visualization manager found");
			return;
		}

		var engineImpl = manager.getEngineImpl();

		if (engineImpl == null) {
			appendLine(out, "Third party backend in use");
			return;
		}

		appendLine(out, "Environments: ").append(engineImpl.environmentStorage().arena.occupancy())
				.append(" / ")
				.append(engineImpl.environmentStorage().arena.capacity());

		appendLine(out, "Light Sections: ").append(engineImpl.lightStorage().arena.occupancy())
				.append(" / ")
				.append(engineImpl.lightStorage().arena.capacity());

		var lut = engineImpl.lightStorage()
				.createLut();

		appendLine(out, "Light LUT Size: ").append(lut.size() * Integer.BYTES)
				.append(" bytes");

		DrawManager<? extends AbstractInstancer<?>> drawManager = engineImpl.drawManager();
		addMeshDebugInfo(out, drawManager);
		addInstancerDebugInfo(out, drawManager);
	}

	private static void addInstancerDebugInfo(StringBuilder out, DrawManager<? extends AbstractInstancer<?>> drawManager) {
		appendHeader2(out, "Instancers");

		var instancers = drawManager.instancers();

		appendLine(out, "Count: ").append(instancers.size());

		{
			IntList meshCountsToSort = new IntArrayList();
			for (var instancerKey : instancers.keySet()) {
				meshCountsToSort.add(instancerKey.model()
						.meshes()
						.size());
			}
			appendPercentiles(out, "Mesh Count Percentiles", meshCountsToSort);
		}

		{
			int totalInstanceCount = 0;
			IntList instanceCountsToSort = new IntArrayList();
			for (var instancer : instancers.values()) {
				var instanceCount = instancer.instanceCount();
				totalInstanceCount += instanceCount;
				instanceCountsToSort.add(instanceCount);
			}
			appendLine(out, "Total Instance Count: ").append(totalInstanceCount);
			appendPercentiles(out, "Instance Count Percentiles", instanceCountsToSort);
		}
	}

	private static void addMeshDebugInfo(StringBuilder out, DrawManager<? extends AbstractInstancer<?>> drawManager) {
		var meshPool = drawManager.meshPool()
				.pooledMeshes();

		appendHeader2(out, "Meshes");

		var numMeshes = meshPool.size();

		appendLine(out, "Count: ").append(numMeshes);

		{
			int totalVertices = 0;
			IntList vertexCountsToSort = new IntArrayList();
			for (var pooledMesh : meshPool) {
				int vertexCount = pooledMesh.vertexCount();

				vertexCountsToSort.add(vertexCount);
				totalVertices += vertexCount;
			}

			appendLine(out, "Total Vertex Count: ").append(totalVertices);
			appendPercentiles(out, "Vertex Count Percentiles", vertexCountsToSort);
		}
	}

	private static void appendPercentiles(StringBuilder out, String prefix, IntList unsortedCounts) {
		var size = unsortedCounts.size();

		if (size == 0) {
			// Append
			appendLine(out, "Empty dataset, no percentiles.");
			return;
		}

		unsortedCounts.sort(IntComparators.NATURAL_COMPARATOR);

		int p10Index = Math.min(size / 10, size - 1);
		int p50Index = Math.min(size / 2, size - 1);
		int p90Index = Math.min(size * 9 / 10, size - 1);

		appendLine(out, prefix).append(":\n   ")
				.append("P10: ")
				.append(unsortedCounts.getInt(p10Index))
				.append(", P50: ")
				.append(unsortedCounts.getInt(p50Index))
				.append(", P90: ")
				.append(unsortedCounts.getInt(p90Index))
				.append(", Max: ")
				.append(unsortedCounts.getInt(size - 1));
	}

	private static void addVisualizationManagerDebugInfo(@Nullable VisualizationManagerImpl manager, StringBuilder out) {
		if (manager == null) {
			out.append('\n');
			appendHeader(out, "Visualization Manager");
			appendLine(out, "No visualization manager found");
			return;
		}

		appendHeader(out, "Client Level Visualization Manager");
		Vec3i renderOrigin = manager.renderOrigin();
		appendLine(out, "Origin: ").append(renderOrigin.getX())
				.append(", ")
				.append(renderOrigin.getY())
				.append(", ")
				.append(renderOrigin.getZ());
		appendLine(out, "Block Entity Visuals: ").append(manager.blockEntities()
				.visualCount());
		appendLine(out, "Entity Visuals: ").append(manager.entities()
				.visualCount());
		appendLine(out, "Effect Visuals: ").append(manager.effects()
				.visualCount());
	}

	private static void addImplDebugInfo(StringBuilder out) {
		appendHeader(out, "Impl");

		appendLine(out, "Flywheel Version: ").append(FlwImplXplat.INSTANCE.getVersionStr());
		appendLine(out, "Backend: ").append(BackendManagerImpl.getBackendString());
		appendLine(out, "Update limiting: ").append(FlwConfig.INSTANCE.limitUpdates() ? "on" : "off");
	}

	private static void addSystemDebugInfo(StringBuilder out) {
		appendHeader(out, "System Info");
		appendLine(out, "Java Version: ").append(System.getProperty("java.version"));
		appendLine(out, "Java VM: ").append(System.getProperty("java.vm.name"))
				.append(" (")
				.append(System.getProperty("java.vm.version"))
				.append(")");
		appendLine(out, "OS: ").append(System.getProperty("os.name"))
				.append(" (")
				.append(System.getProperty("os.arch"))
				.append(")");
		appendLine(out, "Flw CPU Memory: ").append(FlwMemoryTracker.getCpuMemory())
				.append(" bytes");
		appendLine(out, "Flw GPU Memory: ").append(FlwMemoryTracker.getGpuMemory())
				.append(" bytes");
	}

	private static void addOpenGLDebugInfo(StringBuilder out) {
		appendHeader(out, "OpenGL");
		appendLine(out, "Vendor: ").append(GlCompat.GL_VENDOR_STRING);
		appendLine(out, "Renderer: ").append(GlCompat.GL_RENDERER_STRING);
		appendLine(out, "Version: ").append(GlCompat.GL_VERSION_STRING);
		appendLine(out, "Shading Language Version: ").append(GlCompat.GL_SHADING_LANGUAGE_VERSION_STRING);
	}

	public static void addDebugInfo(Minecraft minecraft, List<String> systemInfo) {
		if (minecraft.showOnlyReducedInfo()) {
			return;
		}

		systemInfo.add("");
		systemInfo.add("Flywheel: " + FlwImplXplat.INSTANCE.getVersionStr());
		systemInfo.add("Backend: " + BackendManagerImpl.getBackendString());
		systemInfo.add("Update limiting: " + (FlwConfig.INSTANCE.limitUpdates() ? "on" : "off"));

		VisualizationManager manager = VisualizationManager.get(minecraft.level);
		if (manager != null) {
			systemInfo.add("B: " + manager.blockEntities().visualCount()
					+ ", E: " + manager.entities().visualCount()
					+ ", F: " + manager.effects().visualCount());
			Vec3i renderOrigin = manager.renderOrigin();
			systemInfo.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
		}

		systemInfo.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCpuMemory()) + ", GPU: " + StringUtil.formatBytes(FlwMemoryTracker.getGpuMemory()));
	}
}
