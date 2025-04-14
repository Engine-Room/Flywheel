package dev.engine_room.vanillin.visuals;

import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.EntityVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.baked.BakedModelBuilder;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.vanillin.item.ItemModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;

public class ItemFrameVisual extends AbstractVisual implements EntityVisual<ItemFrame>, SimpleDynamicVisual {
	private static final ModelResourceLocation FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=false");
	private static final ModelResourceLocation MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("item_frame", "map=true");
	private static final ModelResourceLocation GLOW_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=false");
	private static final ModelResourceLocation GLOW_MAP_FRAME_LOCATION = ModelResourceLocation.vanilla("glow_item_frame", "map=true");

	public static final RendererReloadCache<ModelResourceLocation, Model> MODEL_RESOURCE_LOCATION = new RendererReloadCache<>(mrl -> {
		ModelManager modelManager = Minecraft.getInstance()
				.getModelManager()
				.getBlockModelShaper()
				.getModelManager();

		return new BakedModelBuilder(modelManager.getModel(mrl))
				.build();
	});

	private final Matrix4f baseTransform = new Matrix4f();

	private final TransformedInstance frame;
	private final TransformedInstance item;
	private final ItemFrame entity;
	private ModelResourceLocation lastFrameLocation;
	private ItemStack lastItemStack;

	public ItemFrameVisual(VisualizationContext ctx, ItemFrame entity, float partialTick) {
		super(ctx, entity.level(), partialTick);

		this.entity = entity;

		lastItemStack = entity.getItem()
				.copy();

		lastFrameLocation = getFrameModelResourceLoc(entity, lastItemStack);
		var frameModel = MODEL_RESOURCE_LOCATION.get(lastFrameLocation);

		frame = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, frameModel)
				.createInstance();

		frame.setTransform(baseTransform);

		item = ctx.instancerProvider()
				.instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, lastItemStack, ItemDisplayContext.FIXED))
				.createInstance();

		animate(partialTick);
	}

	public static boolean shouldVisualize(ItemFrame entity) {
		// We don't support map rendering, and we can't support exotic item models.
		return !entity.getItem()
				.is(Items.FILLED_MAP) && ItemModels.isSupported(entity.getItem());
	}

	@Override
	public void beginFrame(Context ctx) {
		animate(ctx.partialTick());
	}

	public void animate(float partialTick) {
		var light = LightTexture.pack(getBlockLightLevel(entity.getPos()), getSkyLightLevel(entity.getPos()));

		boolean invisible = entity.isInvisible();

		Direction direction = entity.getDirection();
		var origin = visualizationContext.renderOrigin();

		float d = 0.46875f;

		float x = (float) (entity.getX() - origin.getX() + direction.getStepX() * d);
		float y = (float) (entity.getY() - origin.getY() + direction.getStepY() * d);
		float z = (float) (entity.getZ() - origin.getZ() + direction.getStepZ() * d);

		baseTransform.translation(x, y, z);
		baseTransform.rotateXYZ(Mth.DEG_TO_RAD * entity.getXRot(), Mth.DEG_TO_RAD * (180.0f - entity.getYRot()), 0.0f);

		var stack = entity.getItem();
		var frameLocation = getFrameModelResourceLoc(entity, stack);

		if (frameLocation != lastFrameLocation) {
			visualizationContext.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, MODEL_RESOURCE_LOCATION.get(frameLocation))
					.stealInstance(frame);
			lastFrameLocation = frameLocation;
		}

		frame.setVisible(!invisible);

		frame.setTransform(baseTransform)
				.translate(-0.5f, -0.5f, -0.5f)
				.light(light)
				.setChanged();

		if (!ItemStack.matches(lastItemStack, stack)) {
			lastItemStack = stack.copy();
			visualizationContext.instancerProvider()
					.instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, lastItemStack, ItemDisplayContext.FIXED))
					.stealInstance(item);
		}

		item.setTransform(baseTransform);

		if (invisible) {
			item.translate(0.0F, 0.0F, 0.5F);
		} else {
			item.translate(0.0F, 0.0F, 0.4375F);
		}

		int i = entity.hasFramedMap() ? entity.getRotation() % 4 * 2 : entity.getRotation();

		item.rotateZDegrees(i * 360.0F / 8.0F);

		item.scale(0.5F, 0.5F, 0.5F);

		item.light(getLightVal(15728880, light))
				.setChanged();
	}

	@Override
	public void update(float partialTick) {

	}

	@Override
	protected void _delete() {
		frame.delete();
		item.delete();
	}

	private int getLightVal(int glowLightVal, int regularLightVal) {
		return entity.getType() == EntityType.GLOW_ITEM_FRAME ? glowLightVal : regularLightVal;
	}

	protected int getSkyLightLevel(BlockPos pos) {
		return level.getBrightness(LightLayer.SKY, pos);
	}

	protected int getBlockLightLevelBase(BlockPos pos) {
		return entity.isOnFire() ? 15 : level.getBrightness(LightLayer.BLOCK, pos);
	}

	protected int getBlockLightLevel(BlockPos pos) {
		return entity.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, getBlockLightLevelBase(pos)) : getBlockLightLevelBase(pos);
	}

	public static ModelResourceLocation getFrameModelResourceLoc(ItemFrame entity, ItemStack item) {
		boolean bl = entity.getType() == EntityType.GLOW_ITEM_FRAME;
		if (item.is(Items.FILLED_MAP)) {
			return bl ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION;
		} else {
			return bl ? GLOW_FRAME_LOCATION : FRAME_LOCATION;
		}
	}
}
