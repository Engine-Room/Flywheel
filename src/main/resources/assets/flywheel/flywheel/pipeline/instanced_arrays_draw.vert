#use "flywheel:api/vertex.glsl"

uniform uvec2 _flw_materialID_instancing;

void main() {
    flw_layoutVertex();

    flw_materialVertexID = _flw_materialID_instancing.x;
    flw_materialFragmentID = _flw_materialID_instancing.y;

    FlwInstance i = flw_unpackInstance();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
