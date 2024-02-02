void flw_materialFragment() {
    float toCenter = abs(flw_vertexTexCoord.s - 0.5);

    // multiply by fwidth to get the width of the edge in screen space
    if (flw_defaultLineWidth * fwidth(toCenter) < toCenter) {
        discard;
    }

    flw_fragColor = flw_vertexColor;
}
