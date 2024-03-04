// player.glsl - Holds uniforms for player state.

layout (std140) uniform _FlwPlayerUniforms {
    /** Brightness of the brightest light that the player is holding, 0-1. */
    float flw_heldLight;
    vec4 _flw_eyePos;
    /** The brightness at the player's eye position. */
    vec2 flw_eyeBrightness;
    /** Contains 1 for water, 2 for lava, max-value for any other fluid, 0 for no fluid. */
    uint flw_playerEyeInFluid;
    /** Contains 1 for powder snow, max-value for any other block, 0 for no block. */
    uint flw_playerEyeInBlock;

    uint flw_playerCrouching;
    uint flw_playerSleeping;
    uint flw_playerSwimming;
    uint flw_playerFallFlying;

    uint flw_shiftKeyDown;

    /** 0 = survival, 1 = creative, 2 = adventure, 3 = spectator. */
    uint flw_gameMode;

    /** Alpha is 1 if any team color is present, 0 otherwise. */
    vec4 flw_teamColor;
};

#define flw_eyePos _flw_eyePos.xyz
