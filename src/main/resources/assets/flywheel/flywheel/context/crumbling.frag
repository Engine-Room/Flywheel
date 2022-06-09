#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"

uniform vec2 uFogRange;
uniform vec4 uFogColor;

uniform sampler2D uCrumblingTex;

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

void flw_contextFragment() {
    vec4 color = texture(uCrumblingTex, flattenedPos(flw_vertexPos.xyz, flw_vertexNormal));

    #ifdef ALPHA_DISCARD
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    #ifdef COLOR_FOG
    color = linear_fog(color, flw_distance, uFogRange.x, uFogRange.y, uFogColor);
    #elif defined(FADE_FOG)
    color = linear_fog_fade(color, flw_distance, uFogRange.x, uFogRange.y);
    #endif

    fragColor = color;
}
