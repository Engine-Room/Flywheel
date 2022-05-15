package com.jozufozu.flywheel.core.materials;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.core.materials.model.ModelType;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.jozufozu.flywheel.core.materials.oriented.OrientedType;

public class Materials {
	public static final StructType<ModelData> TRANSFORMED = new ModelType();
	public static final StructType<OrientedData> ORIENTED = new OrientedType();
}
