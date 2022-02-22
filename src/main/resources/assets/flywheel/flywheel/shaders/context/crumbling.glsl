#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

uniform vec2 uTextureScale;
uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;
uniform sampler2D uCrumbling;

uniform vec2 uWindowSize;

#if defined(VERTEX_SHADER)
vec4 FLWVertex(inout Vertex v) {
    FragDistance = cylindrical_distance(v.pos, uCameraPos);

    return uViewProjection * vec4(v.pos, 1.);
}

#elif defined(FRAGMENT_SHADER)

out vec4 fragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    vec4 cr = texture(uCrumbling, texCoords * uTextureScale);
    float diffuseAlpha = texture(uBlockAtlas, texCoords).a;
    cr.a = cr.a * diffuseAlpha;
    return cr;
}

void FLWFinalizeColor(vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif

    if (color.a < 0.1) {
        discard;
    }

    fragColor = color;
}

vec4 FLWLight(vec2 lightCoords) {
    return vec4(1.);
}
#endif
