#use "flywheel:api/vertex.glsl"

void main() {
    flw_layoutVertex();
    FlwInstance i = flw_unpackInstance();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
