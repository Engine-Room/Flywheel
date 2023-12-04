#include "flywheel:api/fragment.glsl"

uniform sampler2D _flw_crumblingTex;

in vec2 crumblingTexCoord;

vec4 crumblingSampleColor;

void flw_beginFragment() {
    crumblingSampleColor = texture(_flw_crumblingTex, crumblingTexCoord);

    // Make the crumbling overlay transparent when the diffuse layer is transparent.
    crumblingSampleColor.a *= flw_fragColor.a;

    if (crumblingSampleColor.a < 0.01) {
        discard;
    }
}

void flw_endFragment() {
    flw_fragColor = crumblingSampleColor;
}
