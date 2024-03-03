void flw_instanceVertex(in FlwInstance i) {
    flw_vertexPos = i.pose * flw_vertexPos;
    flw_vertexNormal = i.normal * flw_vertexNormal;
    flw_vertexColor *= i.color;
    flw_vertexOverlay = i.overlay;
    // Some drivers have a bug where uint over float division is invalid, so use an explicit cast.
    flw_vertexLight = vec2(i.light) / 256.0;
}
