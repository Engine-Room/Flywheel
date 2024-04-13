package com.jozufozu.flywheel.lib.model.part;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.mixin.ModelPartAccessor;
import com.jozufozu.flywheel.lib.model.SimpleMesh;
import com.jozufozu.flywheel.lib.vertex.PosTexNormalVertexView;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class MeshTree {
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
	private static final PoseStack.Pose IDENTITY = new PoseStack().last();
	private static final Map<ModelLayerLocation, MeshTree> MESH_TREES = new ConcurrentHashMap<>();

	private final PartPose initialPose;
	@Nullable
	private final Mesh mesh;
	private final Map<String, MeshTree> children;

	private MeshTree(PartPose initialPose, @Nullable Mesh mesh, Map<String, MeshTree> children) {
		this.initialPose = initialPose;
		this.mesh = mesh;
		this.children = children;
	}

	public static MeshTree get(ModelLayerLocation key) {
		return MESH_TREES.computeIfAbsent(key, MeshTree::convert);
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload(EndClientResourceReloadEvent event) {
		MESH_TREES.values()
				.forEach(MeshTree::delete);
		MESH_TREES.clear();
	}

	public PartPose initialPose() {
		return initialPose;
	}

	@Nullable
	public Mesh mesh() {
		return mesh;
	}

	public Map<String, MeshTree> children() {
		return children;
	}

	@Nullable
	public MeshTree child(String name) {
		return children.get(name);
	}

	public void delete() {
		if (mesh != null) {
			mesh.delete();
		}
		children.values()
				.forEach(MeshTree::delete);
	}

	public static MeshTree convert(ModelLayerLocation location) {
		EntityModelSet entityModels = Minecraft.getInstance()
				.getEntityModels();
		ModelPart modelPart = entityModels.bakeLayer(location);

		return convert(modelPart, THREAD_LOCAL_OBJECTS.get());
	}

	private static MeshTree convert(ModelPart modelPart, ThreadLocalObjects objects) {
		var accessor = cast(modelPart);
		var childModelParts = accessor.flywheel$children();

		Map<String, MeshTree> children = new HashMap<>();

		for (Map.Entry<String, ModelPart> entry : childModelParts.entrySet()) {
			children.put(entry.getKey(), convert(entry.getValue(), objects));
		}

		return new MeshTree(modelPart.getInitialPose(), compile(accessor, objects), children);
	}

	private static Mesh compile(ModelPartAccessor accessor, ThreadLocalObjects objects) {
		var vertexWriter = objects.vertexWriter;

		accessor.flywheel$compile(IDENTITY, vertexWriter, 0, 0, 1.0F, 1.0F, 1.0F, 1.0F);

		var data = vertexWriter.copyDataAndReset();

		return new SimpleMesh(new PosTexNormalVertexView(), data, "source=ModelPartConverter");
	}

	private static ModelPartAccessor cast(ModelPart cube) {
		return (ModelPartAccessor) (Object) cube;
	}

	private static class ThreadLocalObjects {
		public final VertexWriter vertexWriter = new VertexWriter();
	}
}
