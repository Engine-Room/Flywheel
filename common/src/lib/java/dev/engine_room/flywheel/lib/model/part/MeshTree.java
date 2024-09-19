package dev.engine_room.flywheel.lib.model.part;

import java.util.Arrays;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.lib.internal.FlwLibLink;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.ResourceReloadCache;
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
	private static final ResourceReloadCache<ModelLayerLocation, MeshTree> CACHE = new ResourceReloadCache<>(MeshTree::convert);

	@Nullable
	private final Mesh mesh;
	private final PartPose initialPose;
	private final MeshTree[] children;
	private final String[] childNames;

	private MeshTree(@Nullable Mesh mesh, PartPose initialPose, MeshTree[] children, String[] childNames) {
		this.mesh = mesh;
		this.initialPose = initialPose;
		this.children = children;
		this.childNames = childNames;
	}

	public static MeshTree of(ModelLayerLocation layer) {
		return CACHE.get(layer);
	}

	private static MeshTree convert(ModelLayerLocation layer) {
		EntityModelSet entityModels = Minecraft.getInstance()
				.getEntityModels();
		ModelPart modelPart = entityModels.bakeLayer(layer);

		return convert(modelPart, THREAD_LOCAL_OBJECTS.get());
	}

	private static MeshTree convert(ModelPart modelPart, ThreadLocalObjects objects) {
		var modelPartChildren = FlwLibLink.INSTANCE.getModelPartChildren(modelPart);

		String[] childNames = modelPartChildren.keySet()
				.toArray(String[]::new);
		Arrays.sort(childNames);

		MeshTree[] children = new MeshTree[childNames.length];
		for (int i = 0; i < childNames.length; i++) {
			children[i] = convert(modelPartChildren.get(childNames[i]), objects);
		}

		return new MeshTree(compile(modelPart, objects), modelPart.getInitialPose(), children, childNames);
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

	public int childCount() {
		return children.length;
	}

	public MeshTree child(int index) {
		return children[index];
	}

	public String childName(int index) {
		return childNames[index];
	}

	public int childIndex(String name) {
		return Arrays.binarySearch(childNames, name);
	}

	public boolean hasChild(String name) {
		return childIndex(name) >= 0;
	}

	@Nullable
	public MeshTree child(String name) {
		int index = childIndex(name);

		if (index < 0) {
			return null;
		}

		return child(index);
	}

	public MeshTree childOrThrow(String name) {
		MeshTree child = child(name);

		if (child == null) {
			throw new NoSuchElementException("Can't find part " + name);
		}

		return child;
	}

	private static class ThreadLocalObjects {
		public final VertexWriter vertexWriter = new VertexWriter();
	}
}

