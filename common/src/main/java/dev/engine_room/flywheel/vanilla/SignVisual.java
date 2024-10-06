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
import dev.engine_room.flywheel.lib.visual.text.TextLayer;
import dev.engine_room.flywheel.lib.visual.text.TextLayers;
import dev.engine_room.flywheel.lib.visual.text.TextVisual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
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
	private static final Font FONT = Minecraft.getInstance().font;

	private static final ResourceReloadCache<WoodType, ModelTree> SIGN_MODELS = new ResourceReloadCache<>(SignVisual::createSignModel);

	private static final Material MATERIAL = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.texture(Sheets.SIGN_SHEET)
			.backfaceCulling(false)
			.build();

	private final InstanceTree instances;
	private final Matrix4f initialPose;

	// The 8 lines of text we render
	private final TextVisual[] frontTextVisuals = new TextVisual[4];
	private final TextVisual[] backTextVisuals = new TextVisual[4];

	// Need to update these every frame, so just remember which ones are obfuscated
	// Most of the time this will be empty.
	private final List<TextVisual> obfuscated = new ArrayList<>();

	private SignText lastFrontText;
	private SignText lastBackText;

	public SignVisual(VisualizationContext ctx, SignBlockEntity blockEntity, float partialTick) {
		super(ctx, blockEntity, partialTick);

		for (int i = 0; i < 4; i++) {
			frontTextVisuals[i] = new TextVisual(ctx.instancerProvider());
			backTextVisuals[i] = new TextVisual(ctx.instancerProvider());
		}

		var block = (SignBlock) blockState.getBlock();
		WoodType woodType = SignBlock.getWoodType(block);
		var isStanding = block instanceof StandingSignBlock;

		instances = InstanceTree.create(ctx.instancerProvider(), SIGN_MODELS.get(woodType));

		// Maybe use a separate model tree?
		instances.childOrThrow("stick")
				.visible(isStanding);

		var visualPosition = getVisualPosition();
		var signModelRenderScale = getSignModelRenderScale();
		var rotation = -block.getYRotationDegrees(blockState);
		initialPose = new Matrix4f().translate(visualPosition.getX() + 0.5f, visualPosition.getY() + 0.75f * signModelRenderScale, visualPosition.getZ() + 0.5f)
				.rotateY(Mth.DEG_TO_RAD * rotation);

		if (!isStanding) {
			initialPose.translate(0.0f, -0.3125f, -0.4375f);
		}

		// Only apply this to the instances because text gets a separate scaling.
		Matrix4f initialModelPose = new Matrix4f(initialPose).scale(signModelRenderScale, -signModelRenderScale, -signModelRenderScale);
		instances.updateInstancesStatic(initialModelPose);

		lastFrontText = blockEntity.getFrontText();
		lastBackText = blockEntity.getBackText();
		setupText(lastFrontText, true);
		setupText(lastBackText, false);
	}

	private static ModelTree createSignModel(WoodType woodType) {
		return ModelTrees.of(ModelLayers.createSignModelName(woodType), Sheets.getSignMaterial(woodType), MATERIAL);
	}

	@Override
	public void beginFrame(Context ctx) {
		boolean doSetup = false;
		if (lastFrontText != blockEntity.getFrontText()) {
			lastFrontText = blockEntity.getFrontText();
			doSetup = true;
		}

		if (lastBackText != blockEntity.getBackText()) {
			lastBackText = blockEntity.getBackText();
			doSetup = true;
		}

		if (doSetup) {
			// Setup both to make it easier to track obfuscation
			obfuscated.clear();
			setupText(lastFrontText, true);
			setupText(lastBackText, false);
		} else {
			// The is visible check is relatively expensive compared to the boolean checks above,
			// so only do it when it'll actually save some work in obfuscating.
			if (isVisible(ctx.frustum())) {
				obfuscated.forEach(TextVisual::setup);
			}
		}
	}

	@Override
	public void updateLight(float partialTick) {
		int packedLight = computePackedLight();
		instances.traverse(instance -> {
			instance.light(packedLight)
					.setChanged();
		});

		if (!lastFrontText.hasGlowingText()) {
			for (var text : frontTextVisuals) {
				text.light(packedLight);
				text.setup();
			}
		}

		if (!lastBackText.hasGlowingText()) {
			for (var text : backTextVisuals) {
				text.light(packedLight);
				text.setup();
			}
		}
	}

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		instances.traverse(consumer);
	}

	@Override
	protected void _delete() {
		instances.delete();

		for (var text : frontTextVisuals) {
			text.delete();
		}

		for (var text : backTextVisuals) {
			text.delete();
		}
	}

	protected float getSignModelRenderScale() {
		return 0.6666667f;
	}

	protected float getSignTextRenderScale() {
		return 0.6666667f;
	}

	protected Vec3 getTextOffset() {
		return TEXT_OFFSET;
	}

	private void setupText(SignText text, boolean isFrontText) {
		FormattedCharSequence[] textLines = text.getRenderMessages(Minecraft.getInstance()
				.isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = FONT.split(component, blockEntity.getMaxTextLineWidth());
			return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
		});

		List<TextLayer> layers = new ArrayList<>();

		int darkColor = getDarkColor(text);
		int textColor;
		if (text.hasGlowingText()) {
			textColor = text.getColor()
					.getTextColor();

			layers.add(TextLayers.outline(darkColor));
		} else {
			textColor = darkColor;
		}

		layers.add(TextLayers.normal(textColor, Font.DisplayMode.POLYGON_OFFSET, 1));

		var textVisuals = isFrontText ? frontTextVisuals : backTextVisuals;

		int lineHeight = blockEntity.getTextLineHeight();
		int lineDelta = 4 * lineHeight / 2;
		for (int i = 0; i < 4; ++i) {
			FormattedCharSequence textLine = textLines[i];
			float x = (float) (-FONT.width(textLine) / 2);
			float y = i * lineHeight - lineDelta;

			var textVisual = textVisuals[i].layers(layers)
					.text(textLine)
					.pos(x, y)
					.backgroundColor(0);

			var pose = textVisual.pose().set(initialPose);
			if (!isFrontText) {
				pose.rotateY(Mth.PI);
			}
			float scale = 0.015625f * getSignTextRenderScale();
			var textOffset = getTextOffset();
			pose.translate((float) textOffset.x, (float) textOffset.y, (float) textOffset.z);
			pose.scale(scale, -scale, scale);

			if (text.hasGlowingText()) {
				textVisual.light(LightTexture.FULL_BRIGHT);
			}
			// FIXME: incorrect light when going from glowing to non-glowing

			textVisual.setup();

			if (hasObfuscation(textLine)) {
				obfuscated.add(textVisual);
			}
		}
	}

	private static int getDarkColor(SignText signText) {
		int colorArgb = signText.getColor()
				.getTextColor();
		if (colorArgb == DyeColor.BLACK.getTextColor() && signText.hasGlowingText()) {
			return 0xFFF0EBCC;
		}

		int r = (int) ((double) FastColor.ARGB32.red(colorArgb) * 0.4);
		int g = (int) ((double) FastColor.ARGB32.green(colorArgb) * 0.4);
		int b = (int) ((double) FastColor.ARGB32.blue(colorArgb) * 0.4);
		return FastColor.ARGB32.color(0, r, g, b);
	}

	private static boolean hasObfuscation(FormattedCharSequence text) {
		return text.accept((i, s, j) -> s.isObfuscated());
	}
}
