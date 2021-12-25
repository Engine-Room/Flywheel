package com.jozufozu.flywheel.util;

import net.minecraft.client.renderer.RenderType;

/**
 * This is a silly hack that's needed because flywheel does things too different from vanilla.
 *
 * <p>
 *     When a {@link RenderType} is setup, the associated textures are "bound" within RenderSystem, but not actually
 *     bound via opengl. This class provides a helper function to forward the bindings to opengl.
 * </p>
 */
public class TextureBinder {

}
