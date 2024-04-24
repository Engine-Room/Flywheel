// https://stackoverflow.com/a/17479300
uint _flw_hash(in uint x) {
    x += (x << 10u);
    x ^= (x >> 6u);
    x += (x << 3u);
    x ^= (x >> 11u);
    x += (x << 15u);
    return x;
}

vec4 _flw_id2Color(in uint id) {
    uint x = _flw_hash(id);

    return vec4(
    float(x & 0xFFu) / 255.0,
    float((x >> 8u) & 0xFFu) / 255.0,
    float((x >> 16u) & 0xFFu) / 255.0,
    1.
    );
}
