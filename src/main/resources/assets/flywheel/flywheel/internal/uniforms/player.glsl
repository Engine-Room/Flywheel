// player.glsl - Holds uniforms for player state.

layout (std140) uniform _FlwPlayerUniforms {
    vec4 _flw_eyePos;

    /** Alpha is 1 if any team color is present, 0 otherwise. */
    vec4 flw_teamColor;

    /** The brightness at the player's eye position. */
    vec2 flw_eyeBrightness;

    /** Brightness of the brightest light that the player is holding, 0-1. */
    float flw_heldLight;
    /** 0 means no fluid. Use FLW_PLAYER_EYE_IN_FLUID_* defines to detect fluid type. */
    uint flw_playerEyeInFluid;
    /** 0 means no block. Use FLW_PLAYER_EYE_IN_BLOCK_* defines to detect block type. */
    uint flw_playerEyeInBlock;

    uint flw_playerCrouching;
    uint flw_playerSleeping;
    uint flw_playerSwimming;
    uint flw_playerFallFlying;

    uint flw_shiftKeyDown;

    /** 0 = survival, 1 = creative, 2 = adventure, 3 = spectator. */
    uint flw_gameMode;
};

#define flw_eyePos _flw_eyePos.xyz

#define FLW_PLAYER_EYE_IN_FLUID_WATER 1
#define FLW_PLAYER_EYE_IN_FLUID_LAVA 2
#define FLW_PLAYER_EYE_IN_FLUID_UNKNOWN 0xFFFFFFFFu

#define FLW_PLAYER_EYE_IN_BLOCK_POWDER_SNOW 1
#define FLW_PLAYER_EYE_IN_BLOCK_UNKNOWN 0xFFFFFFFFu
