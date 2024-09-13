layout(local_size_x = 256) in;

uniform uint max_mip_level;

/// Generates a hierarchical depth buffer.
/// Based on FidelityFX SPD v2.1 https://github.com/GPUOpen-LibrariesAndSDKs/FidelityFX-SDK/blob/d7531ae47d8b36a5d4025663e731a47a38be882f/sdk/include/FidelityFX/gpu/spd/ffx_spd.h#L528
/// Based on Bevy's more readable implementation https://github.com/JMS55/bevy/blob/ca2c8e63b9562f88c8cd7e1d88a17a4eea20aaf4/crates/bevy_pbr/src/meshlet/downsample_depth.wgsl

shared float[16][16] intermediate_memory;

uint extractBits(uint e, uint offset, uint count) {
    return (e >> offset) & ((1u << count) - 1u);
}

uint insertBits(uint e, uint newbits, uint offset, uint count) {
    uint countMask = ((1u << count) - 1u);
    // zero out the bits we're going to replace first
    return (e & ~(countMask << offset)) | ((newbits & countMask) << offset);
}

uvec2 remap_for_wave_reduction(uint a) {
    return uvec2(
    insertBits(extractBits(a, 2u, 3u), a, 0u, 1u),
    insertBits(extractBits(a, 3u, 3u), extractBits(a, 1u, 2u), 0u, 2u)
    );
}

float reduce_4(vec4 v) {
    return max(max(v.x, v.y), max(v.z, v.w));
}

