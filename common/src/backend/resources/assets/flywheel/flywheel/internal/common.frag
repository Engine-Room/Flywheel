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
flat in uint _flw_instanceID;
#endif

out vec4 _flw_outputColor;

float _flw_diffuseFactor() {
    if (flw_material.diffuse) {
        if (flw_useLightDirections == 1u) {
            return diffuseFromLightDirections(flw_vertexNormal);
        } else {
            if (flw_constantAmbientLight == 1u) {
                return diffuseNether(flw_vertexNormal);
            } else {
                return diffuse(flw_vertexNormal);
            }
        }
    } else {
        return 1.;
    }
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
        color = _flw_id2Color(_flw_instanceID);
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
    }
    #endif

    _flw_outputColor = flw_fogFilter(color);
}
