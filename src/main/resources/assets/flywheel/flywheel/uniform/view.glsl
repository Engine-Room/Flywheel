layout(std140, binding = FLW_UNIFORM_BINDING) uniform flw_view {
    mat4 flw_viewProjection;
    vec4 flw_cameraPos;
    int flw_constantAmbientLight;
};
