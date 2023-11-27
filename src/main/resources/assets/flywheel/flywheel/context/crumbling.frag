#include "flywheel:api/fragment.glsl"

// optimize discard usage
#ifdef ALPHA_DISCARD
#ifdef GL_ARB_conservative_depth
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

uniform vec3 _flw_crumblingBlockPos;

uniform sampler2D flw_diffuseTex;
uniform sampler2D flw_crumblingTex;

in vec2 _flw_crumblingFlip;

out vec4 fragColor;

vec4 flw_crumblingSampleColor;

vec2 flattenedPos(vec3 pos, vec3 normal) {
    pos = pos - _flw_crumblingBlockPos;

    // https://community.khronos.org/t/52861
    vec3 Q1 = dFdx(pos);
    vec3 Q2 = dFdy(pos);
    vec2 st1 = dFdx(flw_vertexTexCoord);
    vec2 st2 = dFdy(flw_vertexTexCoord);

    vec3 T = normalize(Q1*st2.t - Q2*st1.t);
    vec3 B = normalize(-Q1*st2.s + Q2*st1.s);

    mat3 tbn = mat3(T, B, normal);

    // transpose is the same as inverse for orthonormal matrices
    return ((transpose(tbn) * pos).xy + vec2(0.5)) * _flw_crumblingFlip;
}

void flw_initFragment() {
    vec2 crumblingTexCoord = flattenedPos(flw_vertexPos.xyz, flw_vertexNormal);

    flw_crumblingSampleColor = texture(flw_crumblingTex, crumblingTexCoord);
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
