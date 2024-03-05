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
    mat4 flw_viewPrev;
    mat4 flw_projection;
    mat4 flw_projectionInverse;
    mat4 flw_projectionPrev;
    mat4 flw_viewProjection;
    mat4 flw_viewProjectionInverse;
    mat4 flw_viewProjectionPrev;

    vec4 _flw_cameraPos;
    vec4 _flw_cameraPosPrev;
    vec4 _flw_cameraLook;
    vec4 _flw_cameraLookPrev;
    vec2 flw_cameraRot;
    vec2 flw_cameraRotPrev;

    vec2 flw_viewportSize;
    float flw_defaultLineWidth;
    float flw_aspectRatio;
    float flw_viewDistance;

    uint flw_constantAmbientLight;

    uint flw_ticks;
    float flw_partialTick;

    float flw_renderTicks;
    float flw_renderSeconds;

    /** 0 means no fluid. Use FLW_CAMERA_IN_FLUID_* defines to detect fluid type. */
    uint flw_cameraInFluid;
    /** 0 means no block. Use FLW_CAMERA_IN_BLOCK_* defines to detect block type. */
    uint flw_cameraInBlock;

    uint _flw_debugMode;
};

#define flw_cameraPos _flw_cameraPos.xyz
#define flw_cameraLook _flw_cameraLook.xyz
#define flw_cameraPosPrev _flw_cameraPosPrev.xyz
#define flw_cameraLookPrev _flw_cameraLookPrev.xyz

#define FLW_CAMERA_IN_FLUID_WATER 1
#define FLW_CAMERA_IN_FLUID_LAVA 2
#define FLW_CAMERA_IN_FLUID_UNKNOWN 0xFFFFFFFFu

#define FLW_CAMERA_IN_BLOCK_POWDER_SNOW 1
#define FLW_CAMERA_IN_BLOCK_UNKNOWN 0xFFFFFFFFu
