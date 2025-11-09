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

ivec3 flw_renderOrigin;
vec3 flw_cameraPos;
vec3 flw_cameraPosPrev;
vec3 flw_cameraLook;
vec3 flw_cameraLookPrev;
vec2 flw_cameraRot;
vec2 flw_cameraRotPrev;

vec2 flw_viewportSize;
float flw_aspectRatio;
float flw_defaultLineWidth;
float flw_viewDistance;

uint flw_ticks;
float flw_partialTick;
float flw_renderTicks;
float flw_renderSeconds;
float flw_systemSeconds;
uint flw_systemMillis;

/** 0 means no fluid. Use FLW_CAMERA_IN_FLUID_* defines to detect fluid type. */
uint flw_cameraInFluid;
/** 0 means no block. Use FLW_CAMERA_IN_BLOCK_* defines to detect block type. */
uint flw_cameraInBlock;

uint FLW_CAMERA_IN_FLUID_WATER;
uint FLW_CAMERA_IN_FLUID_LAVA;
uint FLW_CAMERA_IN_FLUID_UNKNOWN;

uint FLW_CAMERA_IN_BLOCK_POWDER_SNOW;
uint FLW_CAMERA_IN_BLOCK_UNKNOWN;
