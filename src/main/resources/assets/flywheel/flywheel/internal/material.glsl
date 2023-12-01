#include "flywheel:api/material.glsl"

// Packed format:
// writeMask[2] | cutout[2] | transparency[3] | fog[2] | mip[1] | polygonOffset[1] | backfaceCull[1] | blur[1] | lighting[1] | diffuse[1]

const uint DIFFUSE_MASK = 1u;
const uint LIGHTING_MASK = 1u << 1u;
const uint BLUR_MASK = 1u << 2u;
const uint BACKFACE_CULL_MASK = 1u << 3u;
const uint POLYGON_OFFSET_MASK = 1u << 4u;
const uint MIP_MASK = 1u << 5u;
const uint FOG_MASK = 3u << 6u;
const uint TRANSPARENCY_MASK = 7u << 8u;
const uint CUTOUT_MASK = 3u << 11u;
const uint WRITE_MASK_MASK = 3u << 13u;

void _flw_unpackMaterial(uint m, out FlwMaterial o) {
    o.diffuse = (m & DIFFUSE_MASK) != 0u;
    o.lighting = (m & LIGHTING_MASK) != 0u;
    o.blur = (m & BLUR_MASK) != 0u;
    o.backfaceCull = (m & BACKFACE_CULL_MASK) != 0u;
    o.polygonOffset = (m & POLYGON_OFFSET_MASK) != 0u;
    o.mip = (m & MIP_MASK) != 0u;
    o.fog = (m & FOG_MASK) >> 6;
    o.transparency = (m & TRANSPARENCY_MASK) >> 8;
    o.cutout = (m & CUTOUT_MASK) >> 11;
    o.writeMask = (m & WRITE_MASK_MASK) >> 13;
}
