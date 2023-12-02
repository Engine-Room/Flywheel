#include "flywheel:api/fragment.glsl"

uniform sampler2D flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;

vec4 flw_crumblingSampleColor;

void flw_beginFragment() {
    flw_crumblingSampleColor = texture(flw_crumblingTex, _flw_crumblingTexCoord);

    // Let the other components modify the diffuse color as they normally would.
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;
}

void flw_endFragment() {
    // Still need to discard based on the diffuse color so we don't crumble over empty space.
    if (flw_crumblingSampleColor.a < 0.01) {
        discard;
    }

}
