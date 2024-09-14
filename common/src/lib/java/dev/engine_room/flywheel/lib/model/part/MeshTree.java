package dev.engine_room.flywheel.lib.model.part;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import dev.engine_room.flywheel.lib.vertex.PosTexNormalVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class MeshTree {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
	private static final PoseStack.Pose IDENTITY_POSE = new PoseStack().last();
	private static final Map<ModelLayerLocation, MeshTree> CACHE = new ConcurrentHashMap<>();

	@Nullable
	private final Mesh mesh;
	private final PartPose initialPose;
	@Unmodifiable
	private final Map<String, MeshTree> children;

	private MeshTree(@Nullable Mesh mesh, PartPose initialPose, @Unmodifiable Map<String, MeshTree> children) {
		this.mesh = mesh;
		this.initialPose = initialPose;
		this.children = children;
	}

	public static MeshTree of(ModelLayerLocation layer) {
		return CACHE.computeIfAbsent(layer, MeshTree::convert);
	}

	private static MeshTree convert(ModelLayerLocation layer) {
		EntityModelSet entityModels = Minecraft.getInstance()
				.getEntityModels();
		ModelPart modelPart = entityModels.bakeLayer(layer);

		return convert(modelPart, THREAD_LOCAL_OBJECTS.get());
	}

	private static MeshTree convert(ModelPart modelPart, ThreadLocalObjects objects) {
		var modelPartChildren = FlwLibLink.INSTANCE.getModelPartChildren(modelPart);
		Map<String, MeshTree> children = new HashMap<>();

		modelPartChildren.forEach((name, modelPartChild) -> {
			children.put(name, convert(modelPartChild, objects));
		});

		return new MeshTree(compile(modelPart, objects), modelPart.getInitialPose(), Collections.unmodifiableMap(children));
	}

	@Nullable
	private static Mesh compile(ModelPart modelPart, ThreadLocalObjects objects) {
		if (modelPart.isEmpty()) {
			return null;
		}

		VertexWriter vertexWriter = objects.vertexWriter;
		FlwLibLink.INSTANCE.compileModelPart(modelPart, IDENTITY_POSE, vertexWriter, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		MemoryBlock data = vertexWriter.copyDataAndReset();

		VertexView vertexView = new PosTexNormalVertexView();
		vertexView.load(data);
		return new SimpleQuadMesh(vertexView, "source=MeshTree");
	}

	@Nullable
	public Mesh mesh() {
		return mesh;
	}

	public PartPose initialPose() {
		return initialPose;
	}

	@Unmodifiable
	public Map<String, MeshTree> children() {
		return children;
	}

	public boolean hasChild(String name) {
		return children.containsKey(name);
	}

	@Nullable
	public MeshTree child(String name) {
		return children.get(name);
	}

	public MeshTree childOrThrow(String name) {
		MeshTree child = child(name);

		if (child == null) {
			throw new NoSuchElementException("Can't find part " + name);
		}

		return child;
	}

	public void traverse(Consumer<Mesh> consumer) {
		if (mesh != null) {
			consumer.accept(mesh);
		}
		for (MeshTree child : children.values()) {
			child.traverse(consumer);
		}
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		CACHE.clear();
	}

	private static class ThreadLocalObjects {
		public final VertexWriter vertexWriter = new VertexWriter();
	}
}

