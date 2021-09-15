varying float FragDistance;
uniform vec4 uFogColor;
uniform vec2 uFogRange;

float FLWFogFactor() {
    return (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
}
