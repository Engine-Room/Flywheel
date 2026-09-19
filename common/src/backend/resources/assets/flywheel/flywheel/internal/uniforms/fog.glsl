layout(std140) uniform _FlwFogUniforms {
    vec4 flw_fogColor;
    float flw_fogEnvironmentalStart;
    float flw_fogEnvironmentalEnd;
    float flw_fogRenderDistanceStart;
    float flw_fogRenderDistanceEnd;
    float flw_fogSkyEnd;
    float flw_fogCloudEnd;
};
