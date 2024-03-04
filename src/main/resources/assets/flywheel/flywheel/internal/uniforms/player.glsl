// player.glsl - Holds uniforms for player state.

layout (std140) uniform _FlwPlayerUniforms {
    float flw_heldLight;
    vec4 _flw_eyePos;
    /** The brightness at the player's eye position. */
    vec2 flw_eyeBrightness;
};

#define flw_eyePos _flw_eyePos.xyz
