package dev.engine_room.flywheel.lib.model.part;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class InstanceTree {
	private static final ModelCache<Entry> CACHE = new ModelCache<>(entry -> new SingleMeshModel(entry.mesh(), entry.material()));

	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	public float xScale = ModelPart.DEFAULT_SCALE;
	public float yScale = ModelPart.DEFAULT_SCALE;
	public float zScale = ModelPart.DEFAULT_SCALE;
	public boolean visible = true;
	public boolean skipDraw;

	@Nullable
	public TransformedInstance instance;

	private final Quaternionf rotation = new Quaternionf();
	private final Entry entry;
	private final PartPose initialPose;

	private final Map<String, InstanceTree> children;

	private InstanceTree(InstancerProvider provider, Entry entry, Map<String, InstanceTree> children, PartPose initialPose) {
		this.entry = entry;
		this.children = children;
		this.initialPose = initialPose;

		if (entry.mesh() != null) {
			this.instance = provider.instancer(InstanceTypes.TRANSFORMED, CACHE.get(entry))
					.createInstance();
		}

		this.x = initialPose.x;
		this.y = initialPose.y;
		this.z = initialPose.z;
		this.xRot = initialPose.xRot;
		this.yRot = initialPose.yRot;
		this.zRot = initialPose.zRot;
	}

	public static InstanceTree create(InstancerProvider provider, MeshTree meshTree, Material material) {
		Map<String, InstanceTree> children = new HashMap<>();
		for (Map.Entry<String, MeshTree> child : meshTree.children()
				.entrySet()) {
			var childTree = InstanceTree.create(provider, child.getValue(), material);

			children.put(child.getKey(), childTree);
		}

		return new InstanceTree(provider, new Entry(meshTree.mesh(), material), children, meshTree.initialPose());
	}

	public void offsetPos(Vector3f pOffset) {
		this.x += pOffset.x();
		this.y += pOffset.y();
		this.z += pOffset.z();
	}

	public void offsetRotation(Vector3f pOffset) {
		this.xRot += pOffset.x();
		this.yRot += pOffset.y();
		this.zRot += pOffset.z();
	}

	public void offsetScale(Vector3f pOffset) {
		this.xScale += pOffset.x();
		this.yScale += pOffset.y();
		this.zScale += pOffset.z();
	}

	public void delete() {
		if (instance != null) {
			instance.delete();
		}
		children.values()
				.forEach(InstanceTree::delete);
	}

	public InstanceTree child(String name) {
		return children.get(name);
	}

	public void walkInstances(Consumer<TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance);
		}
		for (InstanceTree child : children.values()) {
			child.walkInstances(consumer);
		}
	}

	public void walkInstances(int i, ObjIntConsumer<TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i);
		}
		for (InstanceTree child : children.values()) {
			child.walkInstances(i, consumer);
		}
	}

	public void walkInstances(int i, int j, ObjIntIntConsumer<TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i, j);
		}
		for (InstanceTree child : children.values()) {
			child.walkInstances(i, j, consumer);
		}
	}

	@FunctionalInterface
	public interface ObjIntIntConsumer<T> {
		void accept(T t, int i, int j);
	}

	public void updateInstances(PoseStack pPoseStack) {
		if (this.visible) {
			pPoseStack.pushPose();
			this.translateAndRotate(pPoseStack);
			if (!this.skipDraw && instance != null) {
				instance.setTransform(pPoseStack.last())
						.setChanged();
			}

			for (InstanceTree modelpart : this.children.values()) {
				modelpart.updateInstances(pPoseStack);
			}

			pPoseStack.popPose();
		}
	}

	public void translateAndRotate(PoseStack pPoseStack) {
		pPoseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
		if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
			pPoseStack.mulPose(rotation.rotationZYX(this.zRot, this.yRot, this.xRot));
		}

		if (this.xScale != 1.0F || this.yScale != 1.0F || this.zScale != 1.0F) {
			pPoseStack.scale(this.xScale, this.yScale, this.zScale);
		}

	}

	private record Entry(@Nullable Mesh mesh, Material material) {
	}
}
