package dev.engine_room.flywheel.lib.model.part;

import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.transform.Affine;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public final class InstanceTree {
	private static final ModelCache<Model.ConfiguredMesh> MODEL_CACHE = new ModelCache<>(entry -> new SingleMeshModel(entry.mesh(), entry.material()));

	private final MeshTree source;
	@Nullable
	private final TransformedInstance instance;
	private final InstanceTree[] children;

	private final Matrix4f poseMatrix;

	private float x;
	private float y;
	private float z;
	private float xRot;
	private float yRot;
	private float zRot;
	private float xScale;
	private float yScale;
	private float zScale;
	@ApiStatus.Experimental
	public boolean visible = true;
	@ApiStatus.Experimental
	public boolean skipDraw;

	private boolean changed;

	private InstanceTree(MeshTree source, @Nullable TransformedInstance instance, InstanceTree[] children) {
		this.source = source;
		this.instance = instance;
		this.children = children;

		if (instance != null) {
			poseMatrix = instance.pose;
		} else {
			poseMatrix = new Matrix4f();
		}

		resetPose();
	}

	private static InstanceTree create(InstancerProvider provider, MeshTree meshTree, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc, String path) {
		InstanceTree[] children = new InstanceTree[meshTree.childCount()];
		String pathSlash = path + "/";

		for (int i = 0; i < meshTree.childCount(); i++) {
			var meshTreeChild = meshTree.child(i);
			String name = meshTree.childName(i);
			children[i] = create(provider, meshTreeChild, meshFinalizerFunc, pathSlash + name);
		}

		Mesh mesh = meshTree.mesh();
		TransformedInstance instance;
		if (mesh != null) {
			Model.ConfiguredMesh configuredMesh = meshFinalizerFunc.apply(path, mesh);
			instance = provider.instancer(InstanceTypes.TRANSFORMED, MODEL_CACHE.get(configuredMesh))
					.createInstance();
		} else {
			instance = null;
		}

		return new InstanceTree(meshTree, instance, children);
	}

	public static InstanceTree create(InstancerProvider provider, MeshTree meshTree, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc) {
		return create(provider, meshTree, meshFinalizerFunc, "");
	}

	public static InstanceTree create(InstancerProvider provider, ModelLayerLocation layer, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc) {
		return create(provider, MeshTree.of(layer), meshFinalizerFunc);
	}

	public static InstanceTree create(InstancerProvider provider, MeshTree meshTree, Material material) {
		return create(provider, meshTree, (path, mesh) -> new Model.ConfiguredMesh(material, mesh));
	}

	public static InstanceTree create(InstancerProvider provider, ModelLayerLocation layer, Material material) {
		return create(provider, MeshTree.of(layer), material);
	}

	@Nullable
	public TransformedInstance instance() {
		return instance;
	}

	public PartPose initialPose() {
		return source.initialPose();
	}

	public int childCount() {
		return children.length;
	}

	public InstanceTree child(int index) {
		return children[index];
	}

	public String childName(int index) {
		return source.childName(index);
	}

	public int childIndex(String name) {
		return source.childIndex(name);
	}

	public boolean hasChild(String name) {
		return childIndex(name) >= 0;
	}

	@Nullable
	public InstanceTree child(String name) {
		int index = childIndex(name);

		if (index < 0) {
			return null;
		}

		return child(index);
	}

	public InstanceTree childOrThrow(String name) {
		InstanceTree child = child(name);

		if (child == null) {
			throw new NoSuchElementException("Can't find part " + name);
		}

		return child;
	}

	public void traverse(Consumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance);
		}
		for (InstanceTree child : children) {
			child.traverse(consumer);
		}
	}

	@ApiStatus.Experimental
	public void traverse(int i, ObjIntConsumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i);
		}
		for (InstanceTree child : children) {
			child.traverse(i, consumer);
		}
	}

	@ApiStatus.Experimental
	public void traverse(int i, int j, ObjIntIntConsumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i, j);
		}
		for (InstanceTree child : children) {
			child.traverse(i, j, consumer);
		}
	}

	public void translateAndRotate(Affine<?> affine, Quaternionf tempQuaternion) {
		affine.translate(x / 16.0F, y / 16.0F, z / 16.0F);

		if (xRot != 0.0F || yRot != 0.0F || zRot != 0.0F) {
			affine.rotate(tempQuaternion.rotationZYX(zRot, yRot, xRot));
		}

		if (xScale != ModelPart.DEFAULT_SCALE || yScale != ModelPart.DEFAULT_SCALE || zScale != ModelPart.DEFAULT_SCALE) {
			affine.scale(xScale, yScale, zScale);
		}
	}

	public void translateAndRotate(PoseStack poseStack, Quaternionf tempQuaternion) {
		translateAndRotate(TransformStack.of(poseStack), tempQuaternion);
	}

	public void translateAndRotate(Matrix4f pose) {
		pose.translate(x / 16.0F, y / 16.0F, z / 16.0F);

		if (xRot != 0.0F || yRot != 0.0F || zRot != 0.0F) {
			pose.rotateZYX(zRot, yRot, xRot);
		}

		if (xScale != ModelPart.DEFAULT_SCALE || yScale != ModelPart.DEFAULT_SCALE || zScale != ModelPart.DEFAULT_SCALE) {
			pose.scale(xScale, yScale, zScale);
		}
	}

	/**
	 * Update the instances in this tree, assuming initialPose changes.
	 *
	 * <p>This is the preferred method for entity visuals, or if you're not sure which you need.
	 *
	 * @param initialPose The root transformation matrix.
	 */
	public void updateInstances(Matrix4fc initialPose) {
		propagateAnimation(initialPose, true);
	}

	/**
	 * Update the instances in this tree, assuming initialPose doesn't change between invocations.
	 *
	 * <p>This is the preferred method for block entity visuals.
	 *
	 * @param initialPose The root transformation matrix.
	 */
	public void updateInstancesStatic(Matrix4fc initialPose) {
		propagateAnimation(initialPose, false);
	}

	/**
	 * Propagate pose transformations to this tree and all its children.
	 *
	 * @param initialPose The root transformation matrix.
	 * @param force       Whether to force the update even if this node's transformations haven't changed.
	 */
	public void propagateAnimation(Matrix4fc initialPose, boolean force) {
		if (!visible) {
			return;
		}

		if (changed || force) {
			poseMatrix.set(initialPose);
			translateAndRotate(poseMatrix);
			force = true;

			if (instance != null && !skipDraw) {
				instance.setChanged();
			}
		}

		for (InstanceTree child : children) {
			child.propagateAnimation(poseMatrix, force);
		}

		changed = false;
	}

	public float xPos() {
		return x;
	}

	public float yPos() {
		return y;
	}

	public float zPos() {
		return z;
	}

	public float xRot() {
		return xRot;
	}

	public float yRot() {
		return yRot;
	}

	public float zRot() {
		return zRot;
	}

	public float xScale() {
		return xScale;
	}

	public float yScale() {
		return yScale;
	}

	public float zScale() {
		return zScale;
	}

	public void xPos(float x) {
		this.x = x;
		setChanged();
	}

	public void yPos(float y) {
		this.y = y;
		setChanged();
	}

	public void zPos(float z) {
		this.z = z;
		setChanged();
	}

	public void pos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		setChanged();
	}

	public void xRot(float xRot) {
		this.xRot = xRot;
		setChanged();
	}

	public void yRot(float yRot) {
		this.yRot = yRot;
		setChanged();
	}

	public void zRot(float zRot) {
		this.zRot = zRot;
		setChanged();
	}

	public void rotation(float xRot, float yRot, float zRot) {
		this.xRot = xRot;
		this.yRot = yRot;
		this.zRot = zRot;
		setChanged();
	}

	public void xScale(float xScale) {
		this.xScale = xScale;
		setChanged();
	}

	public void yScale(float yScale) {
		this.yScale = yScale;
		setChanged();
	}

	public void zScale(float zScale) {
		this.zScale = zScale;
		setChanged();
	}

	public void scale(float xScale, float yScale, float zScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
		setChanged();
	}

	public void offsetPos(float xOffset, float yOffset, float zOffset) {
		x += xOffset;
		y += yOffset;
		z += zOffset;
		setChanged();
	}

	public void offsetXPos(float xOffset) {
		x += xOffset;
		setChanged();
	}

	public void offsetYPos(float yOffset) {
		y += yOffset;
		setChanged();
	}

	public void offsetZPos(float zOffset) {
		z += zOffset;
		setChanged();
	}

	public void offsetPos(Vector3fc offset) {
		offsetPos(offset.x(), offset.y(), offset.z());
	}

	public void offsetRotation(float xOffset, float yOffset, float zOffset) {
		xRot += xOffset;
		yRot += yOffset;
		zRot += zOffset;
		setChanged();
	}

	public void offsetXRot(float xOffset) {
		xRot += xOffset;
		setChanged();
	}

	public void offsetYRot(float yOffset) {
		yRot += yOffset;
		setChanged();
	}

	public void offsetZRot(float zOffset) {
		zRot += zOffset;
		setChanged();
	}

	public void offsetRotation(Vector3fc offset) {
		offsetRotation(offset.x(), offset.y(), offset.z());
	}

	public void offsetScale(float xOffset, float yOffset, float zOffset) {
		xScale += xOffset;
		yScale += yOffset;
		zScale += zOffset;
		setChanged();
	}

	public void offsetXScale(float xOffset) {
		xScale += xOffset;
		setChanged();
	}

	public void offsetYScale(float yOffset) {
		yScale += yOffset;
		setChanged();
	}

	public void offsetZScale(float zOffset) {
		zScale += zOffset;
		setChanged();
	}

	public void offsetScale(Vector3fc offset) {
		offsetScale(offset.x(), offset.y(), offset.z());
	}

	public PartPose storePose() {
		return PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
	}

	public void loadPose(PartPose pose) {
		x = pose.x;
		y = pose.y;
		z = pose.z;
		xRot = pose.xRot;
		yRot = pose.yRot;
		zRot = pose.zRot;
		xScale = ModelPart.DEFAULT_SCALE;
		yScale = ModelPart.DEFAULT_SCALE;
		zScale = ModelPart.DEFAULT_SCALE;
		setChanged();
	}

	public void resetPose() {
		loadPose(source.initialPose());
	}

	public void copyTransform(InstanceTree tree) {
		x = tree.x;
		y = tree.y;
		z = tree.z;
		xRot = tree.xRot;
		yRot = tree.yRot;
		zRot = tree.zRot;
		xScale = tree.xScale;
		yScale = tree.yScale;
		zScale = tree.zScale;
		setChanged();
	}

	public void copyTransform(ModelPart modelPart) {
		x = modelPart.x;
		y = modelPart.y;
		z = modelPart.z;
		xRot = modelPart.xRot;
		yRot = modelPart.yRot;
		zRot = modelPart.zRot;
		xScale = modelPart.xScale;
		yScale = modelPart.yScale;
		zScale = modelPart.zScale;
		setChanged();
	}

	private void setChanged() {
		changed = true;
	}

	public void delete() {
		if (instance != null) {
			instance.delete();
		}
		for (InstanceTree child : children) {
			child.delete();
		}
	}

	@ApiStatus.Experimental
	@FunctionalInterface
	public interface ObjIntIntConsumer<T> {
		void accept(T t, int i, int j);
	}
}
