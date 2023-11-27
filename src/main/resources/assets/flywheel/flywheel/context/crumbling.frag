#include "flywheel:api/fragment.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform sampler2D flw_diffuseTex;
uniform sampler2D flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;

out vec4 fragColor;

vec4 flw_crumblingSampleColor;

void flw_initFragment() {
    flw_crumblingSampleColor = texture(flw_crumblingTex, _flw_crumblingTexCoord);
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);

    // Let the other components modify the diffuse color as they normally would.
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;
}

void flw_contextFragment() {
    vec4 color = flw_fragColor;

    // Still need to discard based on the diffuse color so we don't crumble over empty space.
    if (flw_discardPredicate(color) || flw_crumblingSampleColor.a < 0.01) {
        discard;
    }

    fragColor = flw_fogFilter(flw_crumblingSampleColor);
}
