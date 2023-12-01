const uint LINEAR = 0;
const uint LINEAR_FADE = 1;
const uint NONE = 2;

const uint OPAQUE_TRANSPARENCY = 0;
const uint ADDITIVE_TRANSPARENCY = 1;
const uint LIGHTING_TRANSPARENCY = 2;
const uint GLINT_TRANSPARENCY = 3;
const uint CRUMBLING_TRANSPARENCY = 4;
const uint TRANSLUCENT_TRANSPARENCY = 5;

const uint CUTOUT_OFF = 0;
const uint CUTOUT_EPSILON = 1;
const uint CUTOUT_HALF = 2;

const uint WRITE_MASK_BOTH = 0;
const uint WRITE_MASK_COLOR = 1;
const uint WRITE_MASK_DEPTH = 2;

struct Material {
    bool diffuse;
    bool lighting;
    bool blur;
    bool backfaceCull;
    bool polygonOffset;
    bool mip;

    uint fog;
    uint transparency;
    uint cutout;
    uint writeMask;
};

// Packed format:
// writeMask[2] | cutout[2] | transparency[3] | fog[2] | mip[1] | polygonOffset[1] | backfaceCull[1] | blur[1] | lighting[1] | diffuse[1]

const uint DIFFUSE_MASK = 1;
const uint LIGHTING_MASK = 1 << 1;
const uint BLUR_MASK = 1 << 2;
const uint BACKFACE_CULL_MASK = 1 << 3;
const uint POLYGON_OFFSET_MASK = 1 << 4;
const uint MIP_MASK = 1 << 5;
const uint FOG_MASK = 3 << 6;
const uint TRANSPARENCY_MASK = 7 << 8;
const uint CUTOUT_MASK = 3 << 11;
const uint WRITE_MASK_MASK = 3 << 13;

void unpackMaterial(uint m, out Material o) {
    o.diffuse = (m & DIFFUSE_MASK) != 0;
    o.lighting = (m & LIGHTING_MASK) != 0;
    o.blur = (m & BLUR_MASK) != 0;
    o.backfaceCull = (m & BACKFACE_CULL_MASK) != 0;
    o.polygonOffset = (m & POLYGON_OFFSET_MASK) != 0;
    o.mip = (m & MIP_MASK) != 0;
    o.fog = (m & FOG_MASK) >> 6;
    o.transparency = (m & TRANSPARENCY_MASK) >> 8;
    o.cutout = (m & CUTOUT_MASK) >> 11;
    o.writeMask = (m & WRITE_MASK_MASK) >> 13;
}
