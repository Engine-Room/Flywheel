#use "flywheel:api/vertex.glsl"

uniform uvec2 _flw_materialID_instancing;

void main() {
    flw_layoutVertex();

    _flw_materialVertexID = _flw_materialID_instancing.x;
    _flw_materialFragmentID = _flw_materialID_instancing.y;

    FlwInstance i = _flw_unpackInstance();
    flw_instanceVertex(i);
    flw_materialVertex();
    flw_contextVertex();
}
