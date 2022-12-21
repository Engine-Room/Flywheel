// TODO: inject FLW_UNIFORM_BINDING definitions
layout(std140, binding = FLW_UNIFORM_BINDING) uniform flw_fog {
    vec4 flw_fogColor;
    vec2 flw_fogRange;
    int flw_fogShape;
};
