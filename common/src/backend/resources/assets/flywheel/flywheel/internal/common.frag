#include "flywheel:internal/packed_material.glsl"
#include "flywheel:internal/diffuse.glsl"
#include "flywheel:internal/colorizer.glsl"

// optimize discard usage
#if defined(GL_ARB_conservative_depth) && defined(_FLW_USE_DISCARD)
layout (depth_greater) out float gl_FragDepth;
#endif

#ifdef _FLW_CRUMBLING
uniform sampler2D _flw_crumblingTex;

in vec2 _flw_crumblingTexCoord;
#endif

#ifdef _FLW_DEBUG
flat in uvec2 _flw_ids;
#endif

#ifdef _FLW_OIT

// your first render target which is used to accumulate pre-multiplied color values
layout (location = 0) out vec4 accum;

// your second render target which is used to store pixel revealage
layout (location = 1) out float reveal;

#else

out vec4 _flw_outputColor;

#endif

float _flw_diffuseFactor() {
    if (flw_material.cardinalLightingMode == 2u) {
        return diffuseFromLightDirections(flw_vertexNormal);
    } else if (flw_material.cardinalLightingMode == 1u) {
        if (flw_constantAmbientLight == 1u) {
            return diffuseNether(flw_vertexNormal);
        } else {
            return diffuse(flw_vertexNormal);
        }
    } else {
        return 1.;
    }
}

float linearize_depth(float d, float zNear, float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}

void _flw_main() {
    flw_sampleColor = texture(flw_diffuseTex, flw_vertexTexCoord);
    flw_fragColor = flw_vertexColor * flw_sampleColor;
    flw_fragOverlay = flw_vertexOverlay;
    flw_fragLight = flw_vertexLight;

    flw_materialFragment();

    #ifdef _FLW_CRUMBLING
    vec4 crumblingSampleColor = texture(_flw_crumblingTex, _flw_crumblingTexCoord);

    // Make the crumbling overlay transparent when the fragment color after the material shader is transparent.
    flw_fragColor.rgb = crumblingSampleColor.rgb;
    flw_fragColor.a *= crumblingSampleColor.a;
    #endif

    flw_shaderLight();

    vec4 color = flw_fragColor;

    #ifdef _FLW_USE_DISCARD
    if (flw_discardPredicate(color)) {
        discard;
    }
    #endif

    float diffuseFactor = _flw_diffuseFactor();
    color.rgb *= diffuseFactor;

    if (flw_material.useOverlay) {
        vec4 overlayColor = texelFetch(flw_overlayTex, flw_fragOverlay, 0);
        color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    }

    vec4 lightColor = vec4(1.);
    if (flw_material.useLight) {
        lightColor = texture(flw_lightTex, clamp(flw_fragLight, 0.5 / 16.0, 15.5 / 16.0));
        color *= lightColor;
    }

    #ifdef _FLW_DEBUG
    switch (_flw_debugMode) {
        case 1u:
        color = vec4(flw_vertexNormal * .5 + .5, 1.);
        break;
        case 2u:
        color = _flw_id2Color(_flw_ids.x);
        break;
        case 3u:
        color = vec4(vec2((flw_fragLight * 15.0 + 0.5) / 16.), 0., 1.);
        break;
        case 4u:
        color = lightColor;
        break;
        case 5u:
        color = vec4(flw_fragOverlay / 16., 0., 1.);
        break;
        case 6u:
        color = vec4(vec3(diffuseFactor), 1.);
        break;
        case 7u:
        color = _flw_id2Color(_flw_ids.y);
        break;
    }
    #endif

    color = flw_fogFilter(color);

    color.a = 0.9;

    #ifdef _FLW_OIT

    float depth = linearize_depth(gl_FragCoord.z, _flw_cullData.znear, _flw_cullData.zfar);

    // insert your favorite weighting function here. the color-based factor
    // avoids color pollution from the edges of wispy clouds. the z-based
    // factor gives precedence to nearer surfaces
    //float weight = clamp(pow(min(1.0, color.a * 10.0) + 0.01, 3.0) * 1e8 * pow(1.0 - gl_FragCoord.z * 0.9, 3.0), 1e-2, 3e3);
    float weight = max(min(1.0, max(max(color.r, color.g), color.b) * color.a), color.a) *
    clamp(0.03 / (1e-5 + pow(depth / 200, 4.0)), 1e-2, 3e3);

    // blend func: GL_ONE, GL_ONE
    // switch to pre-multiplied alpha and weight
    accum = vec4(color.rgb * color.a, color.a) * weight;

    // blend func: GL_ZERO, GL_ONE_MINUS_SRC_ALPHA
    reveal = color.a;

    #else

    _flw_outputColor = color;

    #endif
}
