package dev.engine_room.flywheel.vanilla;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.part.InstanceTree;
import dev.engine_room.flywheel.lib.model.part.LoweringVisitor;
import dev.engine_room.flywheel.lib.model.part.ModelTree;
import dev.engine_room.flywheel.lib.visual.ComponentEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;
import dev.engine_room.flywheel.lib.visual.component.FireComponent;
import dev.engine_room.flywheel.lib.visual.component.HitboxComponent;
import dev.engine_room.flywheel.lib.visual.component.ShadowComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MinecartVisual<T extends AbstractMinecart> extends ComponentEntityVisual<T> implements SimpleTickableVisual, SimpleDynamicVisual {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/minecart.png");
	private static final Material MATERIAL = SimpleMaterial.builder()
			.texture(TEXTURE)
			.mipmap(false)
			.build();
	private static final LoweringVisitor VISITOR = LoweringVisitor.materialApplyingVisitor(MATERIAL);

	private final InstanceTree instances;
	@Nullable
	private TransformedInstance contents;

	private final Matrix4fStack stack = new Matrix4fStack(2);

	private BlockState blockState;
	private boolean active;

	public MinecartVisual(VisualizationContext ctx, T entity, float partialTick, ModelLayerLocation layerLocation) {
		super(ctx, entity, partialTick);

		instances = InstanceTree.create(instancerProvider(), ModelTree.of(layerLocation, VISITOR));
		blockState = entity.getDisplayBlockState();
		contents = createContentsInstance();

		addComponent(new ShadowComponent(visualizationContext, entity).radius(0.7f));
		addComponent(new FireComponent(visualizationContext, entity));
		addComponent(new HitboxComponent(visualizationContext, entity));

		updateInstances(partialTick);
		updateLight(partialTick);
	}

	@Nullable
	private TransformedInstance createContentsInstance() {
		RenderShape shape = blockState.getRenderShape();

		if (shape == RenderShape.ENTITYBLOCK_ANIMATED) {
			instances.traverse(instance -> instance.setZeroTransform().setChanged());
			active = false;
			return null;
		}
		active = true;

		if (shape == RenderShape.INVISIBLE) {
			return null;
		}

		return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(blockState))
				.createInstance();
	}

	@Override
	public void tick(TickableVisual.Context context) {
		BlockState displayBlockState = entity.getDisplayBlockState();

		if (displayBlockState != blockState) {
			blockState = displayBlockState;
			if (contents != null) {
				contents.delete();
			}
			contents = createContentsInstance();
		}
	}

	@Override
	public void beginFrame(DynamicVisual.Context context) {
		super.beginFrame(context);

		if (!isVisible(context.frustum())) {
			return;
		}

		// TODO: add proper way to temporarily disable rendering a specific instance
		if (!active) {
			return;
		}

		updateInstances(context.partialTick());
	}

	private void updateInstances(float partialTick) {
		stack.identity();

		double posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

		var renderOrigin = renderOrigin();
		stack.translate((float) (posX - renderOrigin.getX()), (float) (posY - renderOrigin.getY()), (float) (posZ - renderOrigin.getZ()));
		float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

		long randomBits = entity.getId() * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float nudgeX = (((float) (randomBits >> 16 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
		float nudgeY = (((float) (randomBits >> 20 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
		float nudgeZ = (((float) (randomBits >> 24 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
		stack.translate(nudgeX, nudgeY, nudgeZ);

		Vec3 pos = entity.getPos(posX, posY, posZ);
		float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		if (pos != null) {
			Vec3 offset1 = entity.getPosOffs(posX, posY, posZ, 0.3F);
			Vec3 offset2 = entity.getPosOffs(posX, posY, posZ, -0.3F);

			if (offset1 == null) {
				offset1 = pos;
			}

			if (offset2 == null) {
				offset2 = pos;
			}

			stack.translate((float) (pos.x - posX), (float) ((offset1.y + offset2.y) / 2.0D - posY), (float) (pos.z - posZ));
			Vec3 vec = offset2.add(-offset1.x, -offset1.y, -offset1.z);
			if (vec.length() != 0.0D) {
				vec = vec.normalize();
				yaw = (float) (Math.atan2(vec.z, vec.x) * 180.0D / Math.PI);
				pitch = (float) (Math.atan(vec.y) * 73.0D);
			}
		}

		stack.translate(0.0F, 0.375F, 0.0F);
		stack.rotateY((180 - yaw) * Mth.DEG_TO_RAD);
		stack.rotateZ(-pitch * Mth.DEG_TO_RAD);

		float hurtTime = entity.getHurtTime() - partialTick;
		float damage = entity.getDamage() - partialTick;

		if (damage < 0) {
			damage = 0;
		}

		if (hurtTime > 0) {
			stack.rotateX((Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDir()) * Mth.DEG_TO_RAD);
		}

		if (contents != null) {
			int displayOffset = entity.getDisplayOffset();
			stack.pushMatrix();
			stack.scale(0.75F, 0.75F, 0.75F);
			stack.translate(-0.5F, (float) (displayOffset - 8) / 16, 0.5F);
			stack.rotateY(90 * Mth.DEG_TO_RAD);
			updateContents(contents, stack, partialTick);
			stack.popMatrix();
		}

		stack.scale(-1.0F, -1.0F, 1.0F);
		instances.updateInstances(stack);

		// TODO: Use LightUpdatedVisual/ShaderLightVisual if possible.
		updateLight(partialTick);
	}

	protected void updateContents(TransformedInstance contents, Matrix4f pose, float partialTick) {
		contents.setTransform(pose)
				.setChanged();
	}

	public void updateLight(float partialTick) {
		int packedLight = computePackedLight(partialTick);
		instances.traverse(instance -> {
			instance.light(packedLight)
					.setChanged();
		});
		FlatLit.relight(packedLight, contents);
	}

	@Override
	protected void _delete() {
		super._delete();
		instances.delete();
		if (contents != null) {
			contents.delete();
		}
	}

	public static boolean shouldSkipRender(AbstractMinecart minecart) {
		return minecart.getDisplayBlockState().getRenderShape() != RenderShape.ENTITYBLOCK_ANIMATED;
	}
}
