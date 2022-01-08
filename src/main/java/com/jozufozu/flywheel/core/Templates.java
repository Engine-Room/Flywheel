package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.gl.GLSLVersion;
import com.jozufozu.flywheel.core.compile.InstancingTemplateData;
import com.jozufozu.flywheel.core.compile.OneShotTemplateData;
import com.jozufozu.flywheel.core.compile.Template;

public class Templates {

	public static final Template INSTANCING = new Template(GLSLVersion.V330, InstancingTemplateData::new);
	public static final Template ONE_SHOT = new Template(GLSLVersion.V150, OneShotTemplateData::new);
}
