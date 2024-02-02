struct FrustumPlanes {
    vec4 xyX;// <nx.x, px.x, ny.x, py.x>
    vec4 xyY;// <nx.y, px.y, ny.y, py.y>
    vec4 xyZ;// <nx.z, px.z, ny.z, py.z>
    vec4 xyW;// <nx.w, px.w, ny.w, py.w>
    vec2 zX;// <nz.x, pz.x>
    vec2 zY;// <nz.y, pz.y>
    vec2 zZ;// <nz.z, pz.z>
    vec2 zW;// <nz.w, pz.w>
};

layout(std140) uniform _FlwFrameUniforms {
    FrustumPlanes flw_frustumPlanes;

    mat4 flw_viewProjection;
    mat4 flw_viewProjectionInverse;

    vec4 _flw_cameraPos;
    vec4 _flw_cameraLook;
    vec2 flw_cameraRot;

    vec2 flw_viewportSize;
    float flw_defaultLineWidth;

    uint flw_constantAmbientLight;

    uint flw_ticks;
    float flw_partialTick;

    float flw_renderTicks;
    float flw_renderSeconds;
};

#define flw_cameraPos _flw_cameraPos.xyz
#define flw_cameraLook _flw_cameraLook.xyz
