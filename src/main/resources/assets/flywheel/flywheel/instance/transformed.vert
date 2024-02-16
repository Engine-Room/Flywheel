void flw_instanceVertex(in FlwInstance i) {
    flw_vertexPos = i.pose * flw_vertexPos;
    flw_vertexNormal = i.normal * flw_vertexNormal;
    flw_vertexColor = i.color;
    flw_vertexOverlay = i.overlay_light.xy;
    flw_vertexLight = i.overlay_light.zw / 15.0;
}
