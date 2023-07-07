#use "flywheel:context/fog.glsl"

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

uniform vec2 uWindowSize;

#if defined(VERTEX_SHADER)
// Replicates the result of SheetedDecalTextureGenerator.java
vec2 crumblingUV(vec3 pos, vec3 normal) {
    float maxLen = -2;
    int face = 2;

    if (-normal.y > maxLen) {
        maxLen = -normal.y;
        face = 0;
    }
    if (normal.y > maxLen) {
        maxLen = normal.y;
        face = 1;
    }
    if (-normal.z > maxLen) {
        maxLen = -normal.z;
        face = 2;
    }
    if (normal.z > maxLen) {
        maxLen = normal.z;
        face = 3;
    }
    if (-normal.x > maxLen) {
        maxLen = -normal.x;
        face = 4;
    }
    if (normal.x > maxLen) {
        maxLen = normal.x;
        face = 5;
    }

    if (face == 0) {
        return vec2(pos.x, -pos.z);
    } else if (face == 1) {
        return vec2(pos.x, pos.z);
    } else if (face == 3) {
        return vec2(pos.x, -pos.y);
    } else if (face == 4) {
        return vec2(-pos.z, -pos.y);
    } else if (face == 5) {
        return vec2(pos.z, -pos.y);
    } else { // face == 2
        return vec2(-pos.x, -pos.y);
    }
}

vec4 FLWVertex(inout Vertex v) {
    v.texCoords = crumblingUV(v.pos, normalize(v.normal));

    FragDistance = cylindrical_distance(v.pos, uCameraPos);

    return uViewProjection * vec4(v.pos, 1.);
}

#elif defined(FRAGMENT_SHADER)

out vec4 fragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture(uBlockAtlas, texCoords);
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
