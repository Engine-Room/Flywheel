void flw_instanceVertex(in FlwInstance i) {
    // Stretch the quad to the shape of the block the shadow is being cast on,
    // then move it to the correct position.
    flw_vertexPos.xyz = flw_vertexPos.xyz * vec3(i.size.x, 1., i.size.y) + i.pos;

    // Uvs are calculated based on the distance to the entity.
    flw_vertexTexCoord = (flw_vertexPos.xz - i.entityPosXZ) * 0.5 / i.radius + 0.5;

    flw_vertexColor.a = i.alpha;
}
