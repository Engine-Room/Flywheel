#use "flywheel:api/vertex.glsl"

void main() {
    flw_layoutVertex();
    flw_instanceVertex();
    flw_materialVertex();
    flw_contextVertex();
}
