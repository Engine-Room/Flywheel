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

    mat4 flw_view;
    mat4 flw_viewInverse;
    mat4 flw_lastView;
    mat4 flw_projection;
    mat4 flw_projectionInverse;
    mat4 flw_lastProjection;
    mat4 flw_viewProjection;
    mat4 flw_viewProjectionInverse;
    mat4 flw_lastViewProjection;

    vec4 _flw_cameraPos;
    vec4 _flw_cameraLook;
    vec2 flw_cameraRot;
    vec4 _flw_lastCameraPos;
    vec4 _flw_lastCameraLook;
    vec2 flw_lastCameraRot;

    vec2 flw_viewportSize;
    float flw_defaultLineWidth;

    uint flw_constantAmbientLight;

    uint flw_ticks;
    float flw_partialTick;

    float flw_renderTicks;
    float flw_renderSeconds;

    uint _flw_debugMode;
};

#define flw_cameraPos _flw_cameraPos.xyz
#define flw_cameraLook _flw_cameraLook.xyz
#define flw_lastCameraPos _flw_lastCameraPos.xyz
#define flw_lastCameraLook _flw_lastCameraLook.xyz
