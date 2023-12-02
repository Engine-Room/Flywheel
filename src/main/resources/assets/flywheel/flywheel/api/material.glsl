const uint FLW_TRANSPARENCY_OPAQUE = 0u;
const uint FLW_TRANSPARENCY_ADDITIVE = 1u;
const uint FLW_TRANSPARENCY_LIGHTING = 2u;
const uint FLW_TRANSPARENCY_GLINT = 3u;
const uint FLW_TRANSPARENCY_CRUMBLING = 4u;
const uint FLW_TRANSPARENCY_TRANSLUCENT = 5u;

const uint FLW_WRITE_MASK_BOTH = 0u;
const uint FLW_WRITE_MASK_COLOR = 1u;
const uint FLW_WRITE_MASK_DEPTH = 2u;

struct FlwMaterial {
    bool diffuse;
    bool lighting;
    bool blur;
    bool backfaceCull;
    bool polygonOffset;
    bool mip;

    uint writeMask;
    uint transparency;
};
