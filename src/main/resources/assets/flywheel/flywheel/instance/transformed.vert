#use "flywheel:api/vertex.glsl"

layout(location = FLW_INSTANCE_BASE_INDEX + 0) in ivec2 transformed_light;
layout(location = FLW_INSTANCE_BASE_INDEX + 1) in vec4 transformed_color;
layout(location = FLW_INSTANCE_BASE_INDEX + 2) in mat4 transformed_pose;
layout(location = FLW_INSTANCE_BASE_INDEX + 6) in mat3 transformed_normal;

void flw_instanceVertex() {
    flw_vertexPos = transformed_pose * flw_vertexPos;
    flw_vertexNormal = transformed_normal * flw_vertexNormal;
    flw_vertexColor = transformed_color;
    flw_vertexLight = transformed_light / 15.0;
}
