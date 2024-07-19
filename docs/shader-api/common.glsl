/// Get the light at the given world position.
/// This may be interpolated for smooth lighting.
bool flw_light(vec3 worldPos, out vec2 light);

/// Fetches the light value at the given block position.
/// Returns false if the light for the given block is not available.
bool flw_lightFetch(ivec3 blockPos, out vec2 light);
