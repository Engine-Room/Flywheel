package dev.engine_room.vanillin;

import dev.engine_room.vanillin.compose.VisualElement;
import dev.engine_room.vanillin.elements.FireElement;
import dev.engine_room.vanillin.elements.ShadowElement;
import dev.engine_room.vanillin.visuals.BlockDisplayVisual;
import dev.engine_room.vanillin.visuals.MinecartVisual;
import dev.engine_room.vanillin.visuals.TntMinecartVisual;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;

// TODO: A way to get other elements in a visual, likely using these as keys.
public class VisualElements {
	public static final VisualElement<Entity, ShadowElement.Config> SHADOW = ShadowElement::new;
	public static final VisualElement.Unit<Entity> FIRE = FireElement::new;

	// FIXME 1.21.11: port
//	public static final VisualElement.Unit<ItemEntity> ITEM_ENTITY = ItemVisual::new;
//	public static final VisualElement.Unit<Display.ItemDisplay> ITEM_DISPLAY = ItemDisplayVisual::new;
	public static final VisualElement.Unit<Display.BlockDisplay> BLOCK_DISPLAY = BlockDisplayVisual::new;
	// FIXME 1.21.11: port
//	public static final VisualElement.Unit<ItemFrame> ITEM_FRAME = ItemFrameVisual::new;
	public static final VisualElement<AbstractMinecart, ModelLayerLocation> MINECART = MinecartVisual::new;
	public static final VisualElement.Unit<MinecartTNT> TNT_MINECART = TntMinecartVisual::new;

}
