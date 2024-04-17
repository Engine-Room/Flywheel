void flw_materialFragment() {
    float closestEdge = min(flw_vertexTexCoord.s, min(flw_vertexTexCoord.t, min(1. - flw_vertexTexCoord.s, 1. - flw_vertexTexCoord.t)));

    // multiply by fwidth to get the width of the edge in screen space
    if (flw_defaultLineWidth * fwidth(closestEdge) < closestEdge) {
        discard;
    }

    flw_fragColor = flw_vertexColor;
}
