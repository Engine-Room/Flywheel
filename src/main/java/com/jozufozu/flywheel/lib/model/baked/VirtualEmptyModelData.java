package com.jozufozu.flywheel.lib.model.baked;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Model;

import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

/**
 * This model data instance is passed whenever a model is rendered without
 * available in-world context. BakedModel#getModelData can react accordingly
 * and avoid looking for model data itself.
 **/
public class VirtualEmptyModelData {
	// TODO: Remove? Doesn't seem necessary anymore
	public static final ModelData INSTANCE = ModelData.EMPTY;

	public static boolean is(ModelData data) {
		return data == INSTANCE;
	}
}
