#use "flywheel:api/fragment.glsl"
#use "flywheel:util/fog.glsl"

uniform vec2 uFogRange;
uniform vec4 uFogColor;
uniform vec2 uTextureScale;

uniform sampler2D uBlockAtlas;
uniform sampler2D uCrumbling;

in float _flw_diffuse;

out vec4 fragColor;

void flw_contextFragment() {
    vec4 texColor = texture(uBlockAtlas, flw_vertexTexCoord);
    vec4 crumblingColor = texture(uCrumbling, flw_vertexTexCoord * uTextureScale);
    crumblingColor.a *= texColor.a;
    vec4 color = flw_vertexColor * vec4(crumblingColor.rgb * _flw_diffuse, crumblingColor.a);

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
