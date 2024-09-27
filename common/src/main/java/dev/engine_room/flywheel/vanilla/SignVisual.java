package dev.engine_room.flywheel.vanilla;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.SimpleMaterial;
import dev.engine_room.flywheel.lib.model.part.InstanceTree;
import dev.engine_room.flywheel.lib.model.part.ModelTree;
import dev.engine_room.flywheel.lib.model.part.ModelTrees;
import dev.engine_room.flywheel.lib.util.ResourceReloadCache;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.text.SimpleTextLayer;
import dev.engine_room.flywheel.lib.visual.text.TextLayer;
import dev.engine_room.flywheel.lib.visual.text.TextVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

public class SignVisual extends AbstractBlockEntityVisual<SignBlockEntity> implements SimpleDynamicVisual {

	private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.3333333432674408, 0.046666666865348816);

	private static final ResourceReloadCache<WoodType, ModelTree> SIGN_MODELS = new ResourceReloadCache<>(SignVisual::createSignModel);

	private static final Material MATERIAL = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.SIGN_SHEET)
			.backfaceCulling(false)
			.build();

	private final Matrix4f pose = new Matrix4f();
	private final InstanceTree instances;

	// The 8 lines of text we render
	private final TextVisual[] frontText = new TextVisual[4];
	private final TextVisual[] backText = new TextVisual[4];

	private SignText lastFrontText;
	private SignText lastBackText;

	public SignVisual(VisualizationContext ctx, SignBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		for (int i = 0; i < 4; i++) {
			frontText[i] = new TextVisual(ctx.instancerProvider());
			backText[i] = new TextVisual(ctx.instancerProvider());
		}

		var block = (SignBlock) blockState.getBlock();
		WoodType woodType = SignBlock.getWoodType(block);
		var isStanding = block instanceof StandingSignBlock;

		instances = InstanceTree.create(ctx.instancerProvider(), SIGN_MODELS.get(woodType));

		// Maybe use a separate model tree?
		instances.childOrThrow("stick")
				.visible(isStanding);

		var rotation = -block.getYRotationDegrees(blockState);

		var visualPosition = getVisualPosition();
		var signModelRenderScale = this.getSignModelRenderScale();
		pose.translate(visualPosition.getX() + 0.5f, visualPosition.getY() + 0.75f * signModelRenderScale, visualPosition.getZ() + 0.5f)
				.rotateY(Mth.DEG_TO_RAD * rotation);

		if (!(isStanding)) {
			pose.translate(0.0f, -0.3125f, -0.4375f);
		}

		// Only apply this to the instances because text gets a separate scaling.
		instances.scale(signModelRenderScale, -signModelRenderScale, -signModelRenderScale);

		instances.updateInstancesStatic(pose);

		lastFrontText = blockEntity.getFrontText();
		lastBackText = blockEntity.getBackText();
		this.setupText(lastFrontText, frontText, true);
		this.setupText(lastBackText, backText, false);
	}

	@Override
	public void beginFrame(Context ctx) {
		// Need to update every frame if the text is obfuscated

		if (lastFrontText != blockEntity.getFrontText()) {
			lastFrontText = blockEntity.getFrontText();
			this.setupText(lastFrontText, frontText, true);
		}

		if (lastBackText != blockEntity.getBackText()) {
			lastBackText = blockEntity.getBackText();
			this.setupText(lastBackText, backText, false);
		}
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		instances.traverse(consumer);
	}

	@Override
	public void updateLight(float partialTick) {
		int packedLight = computePackedLight();
		instances.traverse(instance -> {
			instance.light(packedLight)
					.setChanged();
		});

		for (var text : frontText) {
			text.light(packedLight);
		}
		for (var text : backText) {
			text.light(packedLight);
		}
	}

	@Override
	protected void _delete() {
		instances.delete();

		for (var text : frontText) {
			text.delete();
		}

		for (var text : backText) {
			text.delete();
		}
	}

	public float getSignModelRenderScale() {
		return 0.6666667f;
	}

	public float getSignTextRenderScale() {
		return 0.6666667f;
	}

	public Vec3 getTextOffset() {
		return TEXT_OFFSET;
	}

	void setupText(SignText text, TextVisual[] dst, boolean isFrontText) {
		FormattedCharSequence[] formattedCharSequences = text.getRenderMessages(Minecraft.getInstance()
				.isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = Minecraft.getInstance().font.split(component, blockEntity.getMaxTextLineWidth());
			return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
		});

		List<TextLayer> layers = new ArrayList<>();

		int darkColor = adjustColor(getDarkColor(text));
		int textColor;
		if (text.hasGlowingText()) {
			textColor = adjustColor(text.getColor()
					.getTextColor());

			layers.add(new SimpleTextLayer.Builder().style(TextLayer.GlyphMeshStyle.OUTLINE)
					.material(TextLayer.GlyphMaterial.SIMPLE)
					.color(TextLayer.GlyphColor.always(darkColor))
					.build());
		} else {
			textColor = darkColor;
		}

		layers.add(new SimpleTextLayer.Builder().style(TextLayer.GlyphMeshStyle.SIMPLE)
				.material(TextLayer.GlyphMaterial.POLYGON_OFFSET)
				.color(TextLayer.GlyphColor.defaultTo(textColor))
				.bias(1)
				.build());

		int lineHeight = blockEntity.getTextLineHeight();
		int lineDelta = 4 * lineHeight / 2;
		for (int m = 0; m < 4; ++m) {
			FormattedCharSequence formattedCharSequence = formattedCharSequences[m];
			float f = (float) -Minecraft.getInstance().font.width(formattedCharSequence) / 2;

			var textVisual = dst[m].content(formattedCharSequence)
					.layers(layers)
					.fullBright(text.hasGlowingText())
					.backgroundColor(0)
					.x(f)
					.y(m * lineHeight - lineDelta);

			var textPose = textVisual.pose();

			textPose.set(pose);

			if (!isFrontText) {
				textPose.rotateY(Mth.PI);
			}
			var offset = getTextOffset();
			float scale = 0.015625f * this.getSignTextRenderScale();
			textPose.translate((float) offset.x, (float) offset.y, (float) offset.z);
			textPose.scale(scale, -scale, scale);

			textVisual.setup();
		}
	}

	private static int adjustColor(int color) {
		if ((color & 0xFC000000) == 0) {
			return color | 0xFF000000;
		}
		return color;
	}

	static int getDarkColor(SignText signText) {
		int i = signText.getColor()
				.getTextColor();
		if (i == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
			return -988212;
		}
		int j = (int) ((double) FastColor.ARGB32.red(i) * 0.4);
		int k = (int) ((double) FastColor.ARGB32.green(i) * 0.4);
		int l = (int) ((double) FastColor.ARGB32.blue(i) * 0.4);
		return FastColor.ARGB32.color(0, j, k, l);
	}

	private static ModelTree createSignModel(WoodType woodType) {
		return ModelTrees.of(ModelLayers.createSignModelName(woodType), Sheets.getSignMaterial(woodType), MATERIAL);
	}
}
