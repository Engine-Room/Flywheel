#include "flywheel:api/fragment.glsl"

uniform sampler2D flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;

vec4 flw_crumblingSampleColor;

void flw_beginFragment() {
    flw_crumblingSampleColor = texture(flw_crumblingTex, _flw_crumblingTexCoord);

    if (flw_crumblingSampleColor.a < 0.01) {
        discard;
    }
}

void flw_endFragment() {
    flw_fragColor = flw_crumblingSampleColor;
}
