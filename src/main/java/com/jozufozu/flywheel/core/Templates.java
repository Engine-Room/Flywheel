package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.backend.pipeline.InstancingTemplateData;
import com.jozufozu.flywheel.backend.pipeline.OneShotTemplateData;
import com.jozufozu.flywheel.backend.pipeline.Template;

public class Templates {

	public static final Template<InstancingTemplateData> INSTANCING = new Template<>(GLSLVersion.V330, InstancingTemplateData::new);
	public static final Template<OneShotTemplateData> ONE_SHOT = new Template<>(GLSLVersion.V150, OneShotTemplateData::new);
}
