#if defined(VERTEX_SHADER)
out float FragDistance;
#elif defined(FRAGMENT_SHADER)
in float FragDistance;
#endif
uniform vec4 uFogColor;
uniform vec2 uFogRange;

float FLWFogFactor() {
    return (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
}
