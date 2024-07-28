// The number of bits each property takes up
const uint _FLW_BLUR_LENGTH = 1u;
const uint _FLW_MIPMAP_LENGTH = 1u;
const uint _FLW_BACKFACE_CULLING_LENGTH = 1u;
const uint _FLW_POLYGON_OFFSET_LENGTH = 1u;
const uint _FLW_DEPTH_TEST_LENGTH = 4u;
const uint _FLW_TRANSPARENCY_LENGTH = 3u;
const uint _FLW_WRITE_MASK_LENGTH = 2u;
const uint _FLW_USE_OVERLAY_LENGTH = 1u;
const uint _FLW_USE_LIGHT_LENGTH = 1u;
const uint _FLW_DIFFUSE_LENGTH = 1u;

// The bit offset of each property
const uint _FLW_BLUR_OFFSET = 0u;
const uint _FLW_MIPMAP_OFFSET = _FLW_BLUR_OFFSET + _FLW_BLUR_LENGTH;
const uint _FLW_BACKFACE_CULLING_OFFSET = _FLW_MIPMAP_OFFSET + _FLW_MIPMAP_LENGTH;
const uint _FLW_POLYGON_OFFSET_OFFSET = _FLW_BACKFACE_CULLING_OFFSET + _FLW_BACKFACE_CULLING_LENGTH;
const uint _FLW_DEPTH_TEST_OFFSET = _FLW_POLYGON_OFFSET_OFFSET + _FLW_POLYGON_OFFSET_LENGTH;
const uint _FLW_TRANSPARENCY_OFFSET = _FLW_DEPTH_TEST_OFFSET + _FLW_DEPTH_TEST_LENGTH;
const uint _FLW_WRITE_MASK_OFFSET = _FLW_TRANSPARENCY_OFFSET + _FLW_TRANSPARENCY_LENGTH;
const uint _FLW_USE_OVERLAY_OFFSET = _FLW_WRITE_MASK_OFFSET + _FLW_WRITE_MASK_LENGTH;
const uint _FLW_USE_LIGHT_OFFSET = _FLW_USE_OVERLAY_OFFSET + _FLW_USE_OVERLAY_LENGTH;
const uint _FLW_DIFFUSE_OFFSET = _FLW_USE_LIGHT_OFFSET + _FLW_USE_LIGHT_LENGTH;

// The bit mask for each property
const uint _FLW_BLUR_MASK = ((1u << _FLW_BLUR_LENGTH) - 1u) << _FLW_BLUR_OFFSET;
const uint _FLW_MIPMAP_MASK = ((1u << _FLW_MIPMAP_LENGTH) - 1u) << _FLW_MIPMAP_OFFSET;
const uint _FLW_BACKFACE_CULLING_MASK = ((1u << _FLW_BACKFACE_CULLING_LENGTH) - 1u) << _FLW_BACKFACE_CULLING_OFFSET;
const uint _FLW_POLYGON_OFFSET_MASK = ((1u << _FLW_POLYGON_OFFSET_LENGTH) - 1u) << _FLW_POLYGON_OFFSET_OFFSET;
const uint _FLW_DEPTH_TEST_MASK = ((1u << _FLW_DEPTH_TEST_LENGTH) - 1u) << _FLW_DEPTH_TEST_OFFSET;
const uint _FLW_TRANSPARENCY_MASK = ((1u << _FLW_TRANSPARENCY_LENGTH) - 1u) << _FLW_TRANSPARENCY_OFFSET;
const uint _FLW_WRITE_MASK_MASK = ((1u << _FLW_WRITE_MASK_LENGTH) - 1u) << _FLW_WRITE_MASK_OFFSET;
const uint _FLW_USE_OVERLAY_MASK = ((1u << _FLW_USE_OVERLAY_LENGTH) - 1u) << _FLW_USE_OVERLAY_OFFSET;
const uint _FLW_USE_LIGHT_MASK = ((1u << _FLW_USE_LIGHT_LENGTH) - 1u) << _FLW_USE_LIGHT_OFFSET;
const uint _FLW_DIFFUSE_MASK = ((1u << _FLW_DIFFUSE_LENGTH) - 1u) << _FLW_DIFFUSE_OFFSET;

// Packed format:
// diffuse[1] | useLight[1] | useOverlay[1] | writeMask[2] | transparency[3] | depthTest[4] | polygonOffset[1] | backfaceCulling[1] | mipmap[1] | blur[1]
void _flw_unpackMaterialProperties(uint p, out FlwMaterial m) {
    m.blur = (p & _FLW_BLUR_MASK) != 0u;
    m.mipmap = (p & _FLW_MIPMAP_MASK) != 0u;
    m.backfaceCulling = (p & _FLW_BACKFACE_CULLING_MASK) != 0u;
    m.polygonOffset = (p & _FLW_POLYGON_OFFSET_MASK) != 0u;
    m.depthTest = (p & _FLW_DEPTH_TEST_MASK) >> _FLW_DEPTH_TEST_OFFSET;
    m.transparency = (p & _FLW_TRANSPARENCY_MASK) >> _FLW_TRANSPARENCY_OFFSET;
    m.writeMask = (p & _FLW_WRITE_MASK_MASK) >> _FLW_WRITE_MASK_OFFSET;
    m.useOverlay = (p & _FLW_USE_OVERLAY_MASK) != 0u;
    m.useLight = (p & _FLW_USE_LIGHT_MASK) != 0u;
    m.diffuse = (p & _FLW_DIFFUSE_MASK) != 0u;
}

void _flw_unpackUint2x16(uint s, out uint hi, out uint lo) {
    hi = (s >> 16) & 0xFFFFu;
    lo = s & 0xFFFFu;
}

void _flw_unpackUint3x10(uint s, out uint hi, out uint mi, out uint lo) {
    hi = (s >> 20) & 0x3FFu;
    mi = (s >> 10) & 0x3FFu;
    lo = s & 0x3FFu;
}
