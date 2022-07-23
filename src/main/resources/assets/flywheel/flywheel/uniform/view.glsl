
layout(std140, binding = 0) uniform flw_view {
    mat4 flw_viewProjection;
    vec4 flw_cameraPos;
    int flw_constantAmbientLight;
};
