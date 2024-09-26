void flw_instanceVertex(in FlwInstance i) {
    uint vertexInGlyph = flw_vertexId % 4;

    uint yIndex = ((vertexInGlyph + 1u) >> 1u) & 1u;

    flw_vertexPos = i.pose * flw_vertexPos;

    flw_vertexTexCoord.s = i.u0u1v0v1[(vertexInGlyph & 2u) >> 1u];
    flw_vertexTexCoord.t = i.u0u1v0v1[2u + yIndex];

    flw_vertexColor = i.color;

    // Some drivers have a bug where uint over float division is invalid, so use an explicit cast.
    flw_vertexLight = vec2(i.light) / 256.0;
}
