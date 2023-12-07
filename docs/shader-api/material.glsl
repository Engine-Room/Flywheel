const uint FLW_MAT_DEPTH_TEST_OFF = 0u;
const uint FLW_MAT_DEPTH_TEST_NEVER = 1u;
const uint FLW_MAT_DEPTH_TEST_LESS = 2u;
const uint FLW_MAT_DEPTH_TEST_EQUAL = 3u;
const uint FLW_MAT_DEPTH_TEST_LEQUAL = 4u;
const uint FLW_MAT_DEPTH_TEST_GREATER = 5u;
const uint FLW_MAT_DEPTH_TEST_NOTEQUAL = 6u;
const uint FLW_MAT_DEPTH_TEST_GEQUAL = 7u;
const uint FLW_MAT_DEPTH_TEST_ALWAYS = 8u;

const uint FLW_MAT_TRANSPARENCY_OPAQUE = 0u;
const uint FLW_MAT_TRANSPARENCY_ADDITIVE = 1u;
const uint FLW_MAT_TRANSPARENCY_LIGHTNING = 2u;
const uint FLW_MAT_TRANSPARENCY_GLINT = 3u;
const uint FLW_MAT_TRANSPARENCY_CRUMBLING = 4u;
const uint FLW_MAT_TRANSPARENCY_TRANSLUCENT = 5u;

const uint FLW_MAT_WRITE_MASK_COLOR_DEPTH = 0u;
const uint FLW_MAT_WRITE_MASK_COLOR = 1u;
const uint FLW_MAT_WRITE_MASK_DEPTH = 2u;

struct FlwMaterial {
    bool blur;
    bool mipmap;
    bool backfaceCulling;
    bool polygonOffset;
    uint depthTest;
    uint transparency;
    uint writeMask;
    bool useOverlay;
    bool useLight;
    bool diffuse;
};
