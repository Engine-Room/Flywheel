#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"
#use "flywheel:uniform/fog.glsl"

uniform sampler2D flw_diffuseTex;

out vec4 fragColor;

vec2 flattenedPos(vec3 pos, vec3 normal) {
    pos -= floor(pos) + vec3(0.5);

    float sinYRot = -normal.x;
    vec2 XZ = normal.xz;
    float sqLength = dot(XZ, XZ);
    if (sqLength > 0) {
        sinYRot *= inversesqrt(sqLength);
        sinYRot = clamp(sinYRot, -1, 1);
    }

    vec3 tangent = vec3(sqrt(1 - sinYRot * sinYRot) * (normal.z < 0 ? -1 : 1), 0, sinYRot);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);

    // transpose is the same as inverse for orthonormal matrices
    return (transpose(tbn) * pos).xy + vec2(0.5);
}

void flw_initFragment() {
    flw_sampleColor = texture(flw_diffuseTex, flattenedPos(flw_vertexPos.xyz, flw_vertexNormal));
    // Crumbling ignores vertex colors
    flw_fragColor = flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;
}

void flw_contextFragment() {
    vec4 color = flw_fragColor;

    #ifdef ALPHA_DISCARD
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    #ifdef COLOR_FOG
    color = linear_fog(color, flw_distance, flw_fogRange.x, flw_fogRange.y, flw_fogColor);
    #elif defined(FADE_FOG)
    color = linear_fog_fade(color, flw_distance, flw_fogRange.x, flw_fogRange.y);
    #endif

    fragColor = color;
}
