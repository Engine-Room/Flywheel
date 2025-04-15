package dev.engine_room.vanillin.item;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.Materials;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.vertex.FullVertexView;
import dev.engine_room.vanillin.Vanillin;
import dev.engine_room.vanillin.VanillinXplat;
import dev.engine_room.vanillin.mixin.item.ItemOverridesAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;

public class ItemModels {
	public static final TagKey<Item> NO_INSTANCING = TagKey.create(Registries.ITEM, Vanillin.rl("no_instancing"));

	private static final Model EMPTY_MODEL = new SimpleModel(List.of());
	private static final RendererReloadCache<BakedMeshKey, Mesh> MESH_CACHE = new RendererReloadCache<>(key -> bakeMesh(key.model(), key.displayContext()));
	private static final RendererReloadCache<BakedModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(key -> bakeModel(key.model(), key.displayContext(), key.material(), key.foil()));

	private static final ModelResourceLocation TRIDENT_MODEL = ModelResourceLocation.vanilla("trident", "inventory");
	private static final ModelResourceLocation SPYGLASS_MODEL = ModelResourceLocation.vanilla("spyglass", "inventory");

	private static final @Nullable Direction[] DIRECTIONS = new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

	private static final Set<ResourceLocation> ALLOWED_OVERRIDES = new HashSet<>();

	static {
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("lefthanded"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("cooldown"));
		ALLOWED_OVERRIDES.add(ItemModelGenerators.TRIM_TYPE_PREDICATE_ID);
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("custom_model_data"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("pull"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("brushing"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("pulling"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("filled"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("charged"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("firework"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("broken"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("cast"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("blocking"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("throwing"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("level"));
		ALLOWED_OVERRIDES.add(ResourceLocation.withDefaultNamespace("tooting"));
	}

	public static boolean isSupported(ItemStack stack) {
		return !stack.is(NO_INSTANCING) && doesNotHaveItemColors(stack.getItem()) && isSupported(getModel(stack));
	}

	private static boolean doesNotHaveItemColors(Item item) {
		return VanillinXplat.INSTANCE.itemColors(item) == null;
	}

	public static BakedModel getModel(ItemStack stack) {
		return Minecraft.getInstance()
				.getItemRenderer()
				.getItemModelShaper()
				.getItemModel(stack);
	}

	@Nullable
	public static BakedModel getActualBakedModel(@Nullable ClientLevel clientLevel, ItemStack itemStack, ItemDisplayContext displayContext) {
		if (itemStack.isEmpty()) {
			return null;
		}

		var baseModel = getModel(itemStack);
		var overrides = baseModel.getOverrides();
		var model = overrides.resolve(baseModel, itemStack, clientLevel, null, 0);

		if (model == null) {
			model = baseModel;
		}

		boolean notEquipped = displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND || displayContext == ItemDisplayContext.FIXED;
		if (model.isCustomRenderer() || itemStack.is(Items.TRIDENT) && !notEquipped) {
			return null;
		}

		if (notEquipped) {
			if (itemStack.is(Items.TRIDENT)) {
				model = Minecraft.getInstance()
						.getItemRenderer()
						.getItemModelShaper()
						.getModelManager()
						.getModel(TRIDENT_MODEL);
			} else if (itemStack.is(Items.SPYGLASS)) {
				model = Minecraft.getInstance()
						.getItemRenderer()
						.getItemModelShaper()
						.getModelManager()
						.getModel(SPYGLASS_MODEL);
			}
		}

		return model;
	}

	public static boolean isSupported(BakedModel model) {
		if (model.isCustomRenderer()) {
			return false;
		}

		var overrides = model.getOverrides();
		if (overrides != ItemOverrides.EMPTY) {
			var properties = ((ItemOverridesAccessor) overrides).vanillin$properties();

			for (var property : properties) {
				if (!ALLOWED_OVERRIDES.contains(property)) {
					return false;
				}
			}
		}

		// Check for class equality rather than instanceof to ensure subclasses are *not* handled by vanillin.
		Class<? extends BakedModel> c = model.getClass();
		if (!(c == SimpleBakedModel.class || c == MultiPartBakedModel.class || c == WeightedBakedModel.class)) {
			return false;
		}

		return true;
	}

	public static Model get(Level level, ItemStack itemStack, ItemDisplayContext displayContext) {
		boolean cull = displayContext == ItemDisplayContext.GUI || displayContext.firstPerson() || !(itemStack.getItem() instanceof BlockItem block) || !(block.getBlock() instanceof HalfTransparentBlock) && !(block.getBlock() instanceof StainedGlassPaneBlock);
		var material = ModelUtil.getItemMaterial(ItemBlockRenderTypes.getRenderType(itemStack, cull));

		if (material == null) {
			material = Materials.TRANSLUCENT_ENTITY;
		}

		if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
			material = SimpleMaterial.builderOf(material)
					.transparency(Transparency.ORDER_INDEPENDENT)
					.build();
		}

		// Visuals all hold references to Level so use that as the parameter type for convenience and cast here.
		ClientLevel clientLevel = (level instanceof ClientLevel) ? (ClientLevel) level : null;

		BakedModel model = getActualBakedModel(clientLevel, itemStack, displayContext);

		if (model == null) {
			return EMPTY_MODEL;
		}

		return MODEL_CACHE.get(new BakedModelKey(model, displayContext, material, itemStack.hasFoil()));
	}

	public static Model bakeModel(BakedModel model, ItemDisplayContext displayContext, Material material, boolean foil) {
		var mesh = MESH_CACHE.get(new BakedMeshKey(model, displayContext));

		if (foil) {
			return new SimpleModel(List.of(new Model.ConfiguredMesh(material, mesh), new Model.ConfiguredMesh(Materials.GLINT, mesh)));
		} else {
			return new SingleMeshModel(mesh, material);
		}
	}

	public static Mesh bakeMesh(BakedModel model, ItemDisplayContext displayContext) {
		boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

		var poseStack = new PoseStack();

		model.getTransforms()
				.getTransform(displayContext)
				.apply(leftHand, poseStack);
		poseStack.translate(-0.5f, -0.5f, -0.5f);

		RandomSource randomSource = RandomSource.create();

		// Write all BakedQuads into a list first to get a count so we can allocate memory ahead of time.
		List<BakedQuad> allQuads = new ArrayList<>();
		for (Direction value : DIRECTIONS) {
			randomSource.setSeed(42L);
			allQuads.addAll(model.getQuads(null, value, randomSource));
		}

		int vertexCount = allQuads.size() * 4;
		var memoryBlock = MemoryBlock.mallocTracked(vertexCount * FullVertexView.STRIDE);
		var meshVertices = new FullVertexView();

		meshVertices.nativeMemoryOwner(memoryBlock);
		meshVertices.ptr(memoryBlock.ptr());
		meshVertices.vertexCount(vertexCount);

		Vector4f position = new Vector4f();
		Vector3f normal = new Vector3f();

		Matrix4f poseMatrix = poseStack.last()
				.pose();
		Matrix3f normalMatrix = poseStack.last()
				.normal();

		try (MemoryStack memoryStack = MemoryStack.stackPush();) {
			ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
			IntBuffer intBuffer = byteBuffer.asIntBuffer();

			int vertex = 0;

			for (BakedQuad quad : allQuads) {
				SodiumAnimatedTextureCompat.add(quad.getSprite());

				int[] js = quad.getVertices();
				var direction = quad.getDirection();

				normal.set(direction.getStepX(), direction.getStepY(), direction.getStepZ());
				normal.mul(normalMatrix);

				int j = js.length / 8;

				for (int k = 0; k < j; ++k) {
					intBuffer.clear();
					intBuffer.put(js, k * 8, 8);

					position.set(byteBuffer.getFloat(0), byteBuffer.getFloat(4), byteBuffer.getFloat(8), 1.0f);
					position.mul(poseMatrix);

					// We could probably handle item colors here.
					meshVertices.x(vertex, position.x());
					meshVertices.y(vertex, position.y());
					meshVertices.z(vertex, position.z());
					meshVertices.r(vertex, 1.0f);
					meshVertices.g(vertex, 1.0f);
					meshVertices.b(vertex, 1.0f);
					meshVertices.a(vertex, 1.0f);
					meshVertices.u(vertex, byteBuffer.getFloat(16));
					meshVertices.v(vertex, byteBuffer.getFloat(20));
					meshVertices.overlay(vertex, OverlayTexture.NO_OVERLAY);
					meshVertices.light(vertex, 0);
					meshVertices.normalX(vertex, normal.x());
					meshVertices.normalY(vertex, normal.y());
					meshVertices.normalZ(vertex, normal.z());

					vertex++;
				}
			}
		}

		return new SimpleQuadMesh(meshVertices);
	}

	public record BakedModelKey(BakedModel model, ItemDisplayContext displayContext, Material material, boolean foil) {

	}

	public record BakedMeshKey(BakedModel model, ItemDisplayContext displayContext) {

	}
}
