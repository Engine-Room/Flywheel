// options.glsl - Houses uniforms for many of the game's settings, focusing on video and accessibility settings.

layout(std140) uniform _FlwOptionsUniforms {
    float flw_brightnessOption;
    uint flw_fovOption;
    float flw_distortionOption;
    float flw_glintSpeedOption;
    float flw_glintStrengthOption;
    uint flw_biomeBlendOption;
    uint flw_smoothLightingOption;
    uint flw_viewBobbingOption;

    uint flw_highContrastOption;
    float flw_textBackgroundOpacityOption;
    uint flw_textBackgroundForChatOnlyOption;
    float flw_darknessPulsingOption;
    float flw_damageTiltOption;
    uint hideLightningFlashesOption;
};
