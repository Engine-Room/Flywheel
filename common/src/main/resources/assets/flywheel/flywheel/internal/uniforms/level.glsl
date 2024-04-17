layout(std140) uniform _FlwLevelUniforms {
    vec4 flw_skyColor;
    vec4 flw_cloudColor;

    /** The current day number of the level. */
    uint flw_levelDay;
    /** The current fraction of the current day that has elapsed. */
    float flw_timeOfDay;

    uint flw_levelHasSkyLight;

    float flw_sunAngle;

    float flw_moonBrightness;
    /** There are normally only 8 moon phases. */
    uint flw_moonPhase;

    uint flw_isRaining;
    float flw_rainLevel;
    uint flw_isThundering;
    float flw_thunderLevel;

    float flw_skyDarken;

    uint flw_constantAmbientLight;

    /** Use FLW_DIMENSION_* ids to determine the dimension. May eventually be implemented for custom dimensions. */
    uint flw_dimension;
};

#define FLW_DIMENSION_OVERWORLD 0
#define FLW_DIMENSION_NETHER 1
#define FLW_DIMENSION_END 2
#define FLW_DIMENSION_UNKNOWN 0xFFFFFFFFu
