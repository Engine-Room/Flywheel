package com.jozufozu.flywheel.core.structs;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.core.structs.model.ModelData;
import com.jozufozu.flywheel.core.structs.model.ModelType;
import com.jozufozu.flywheel.core.structs.oriented.OrientedData;
import com.jozufozu.flywheel.core.structs.oriented.OrientedType;

public class StructTypes {
	public static final StructType<ModelData> MODEL = new ModelType();
	public static final StructType<OrientedData> ORIENTED = new OrientedType();
}
