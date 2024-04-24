void flw_transformBoundingSphere(in FlwInstance i, inout vec3 center, inout float radius) {
    // We can just ignore the base center/radius.
    center = i.pos + vec3(i.size.x * 0.5, 0., i.size.y * 0.5);
    radius = length(i.size) * 0.5;
}
