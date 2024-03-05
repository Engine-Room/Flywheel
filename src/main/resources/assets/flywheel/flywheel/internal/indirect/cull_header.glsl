#include "flywheel:internal/uniforms/frame.glsl"

// Fog doesn't seem like a valid thing to query during the cull pass. Other uniforms added in the
// future may also be excluded, and we'll have to document each one.
// #include "flywheel:internal/uniforms/fog.glsl"

#include "flywheel:internal/uniforms/options.glsl"
#include "flywheel:internal/uniforms/player.glsl"
#include "flywheel:internal/uniforms/level.glsl"
