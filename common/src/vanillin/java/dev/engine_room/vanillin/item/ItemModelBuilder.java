package dev.engine_room.vanillin.item;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.SimpleModel;
import dev.engine_room.flywheel.lib.model.SimpleQuadMesh;
import dev.engine_room.flywheel.lib.vertex.NoOverlayVertexView;
import dev.engine_room.flywheel.lib.vertex.VertexView;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemModelBuilder {
	public static final Comparator<Model.ConfiguredMesh> GLINT_LAST = (a, b) -> {
		if (a.material()
				.transparency() == b.material()
				.transparency()) {
			return 0;
		}
		return a.material()
				.transparency() == Transparency.GLINT ? 1 : -1;
	};

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final ItemStack itemStack;
	private final BakedModel model;
	@Nullable
	private PoseStack poseStack;
	@Nullable
	private ItemDisplayContext displayContext;
	private boolean leftHand;
	@Nullable
	private Function<RenderType, Material> materialFunc;

	public ItemModelBuilder(ItemStack itemStack, BakedModel model) {
		this.itemStack = itemStack;
		this.model = model;
	}

	public ItemModelBuilder poseStack(PoseStack poseStack) {
		this.poseStack = poseStack;
		return this;
	}

	public ItemModelBuilder displayContext(ItemDisplayContext displayContext) {
		this.displayContext = displayContext;
		return this;
	}

	public ItemModelBuilder leftHand(boolean leftHand) {
		this.leftHand = leftHand;
		return this;
	}

	public ItemModelBuilder materialFunc(Function<RenderType, Material> materialFunc) {
		this.materialFunc = materialFunc;
		return this;
	}

	public SimpleModel build() {
		if (displayContext == null) {
			displayContext = ItemDisplayContext.GROUND;
		}
		if (materialFunc == null) {
			materialFunc = ModelUtil::getItemMaterial;
		}

		ArrayList<Model.ConfiguredMesh> out = new ArrayList<>();

		bufferItem(model, itemStack, displayContext, leftHand, poseStack, (renderType, data) -> {
			Material material = materialFunc.apply(renderType);

			if (material != null) {
				// This is a hack to give translucent block models OIT.
				// TODO: Non-block item models use translucent transparency. In order for the glint effect to work
				//  we can't use OIT because OIT renders after glint transparency. It would be very nice to have OIT on
				//  e.g. stained glass items, but we cannot sacrifice the glint effect. Maybe we can do some more
				//  involved check here to decide based on the item stack, or maybe we can hook up the glint effect
				//  directly to OIT.
				if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
					material = SimpleMaterial.builderOf(material)
							.transparency(Transparency.ORDER_INDEPENDENT)
							.build();
				}
				var mesh = blockVerticesToMesh(data, "source=ItemModelBuilder," + "itemStack=" + itemStack + ",renderType=" + renderType);
				if (mesh.vertexCount() != 0) {
					out.add(new Model.ConfiguredMesh(material, mesh));
				}
			}
		});

		out.sort(GLINT_LAST);

		return new SimpleModel(ImmutableList.copyOf(out));
	}

	public static void bufferItem(BakedModel model, ItemStack stack, ItemDisplayContext displayContext, boolean leftHand, @Nullable PoseStack poseStack, ItemMeshEmitter.ResultConsumer consumer) {
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}

		var emitterSource = objects.emitterSource;
		emitterSource.resultConsumer(consumer);

		Minecraft.getInstance()
				.getItemRenderer()
				.render(stack, displayContext, leftHand, poseStack, emitterSource, 0, OverlayTexture.NO_OVERLAY, model);

		emitterSource.end();
	}

	public static SimpleQuadMesh blockVerticesToMesh(MeshData data, @Nullable String meshDescriptor) {
		MeshData.DrawState drawState = data.drawState();
		int vertexCount = drawState.vertexCount();
		long srcStride = drawState.format()
			.getVertexSize();

		VertexView vertexView = new NoOverlayVertexView();
		long dstStride = vertexView.stride();

		ByteBuffer src = data.vertexBuffer();
		MemoryBlock dst = MemoryBlock.mallocTracked((long) vertexCount * dstStride);
		long srcPtr = MemoryUtil.memAddress(src);
		long dstPtr = dst.ptr();
		// The first 31 bytes of each vertex in a block vertex buffer are guaranteed to contain the same data in the
		// same order regardless of whether the format is extended by mods like Iris or OptiFine. Copy these bytes and
		// ignore the rest.
		long bytesToCopy = Math.min(dstStride, 31);

		for (int i = 0; i < vertexCount; i++) {
			// It is safe to copy bytes directly since the NoOverlayVertexView uses the same memory layout as the first
			// 31 bytes of the block vertex format, vanilla or otherwise.
			MemoryUtil.memCopy(srcPtr + srcStride * i, dstPtr + dstStride * i, bytesToCopy);
		}

		vertexView.ptr(dstPtr);
		vertexView.vertexCount(vertexCount);
		vertexView.nativeMemoryOwner(dst);

		return new SimpleQuadMesh(vertexView, meshDescriptor);
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final MeshEmitterSource emitterSource = new MeshEmitterSource();
	}

	private static class MeshEmitterSource implements MultiBufferSource {
		private final Map<RenderType, ItemMeshEmitter> emitters = new Reference2ReferenceOpenHashMap<>();
		private final Set<ItemMeshEmitter> active = new ReferenceArraySet<>();

		@Nullable
		private ItemMeshEmitter.ResultConsumer resultConsumer;

		@Override
		public VertexConsumer getBuffer(RenderType renderType) {
			var out = emitters.computeIfAbsent(renderType, ItemMeshEmitter::new);

			if (active.add(out)) {
				out.prepare(resultConsumer);
			}

			return out.getBuffer();
		}

		public void end() {
			for (var emitter : active) {
				emitter.end();
			}

			active.clear();

			resultConsumer = null;
		}

		public void resultConsumer(ItemMeshEmitter.ResultConsumer resultConsumer) {
			this.resultConsumer = resultConsumer;
		}
	}
}
