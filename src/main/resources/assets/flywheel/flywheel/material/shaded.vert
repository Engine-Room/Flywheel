#use "flywheel:api/vertex.glsl"
#use "flywheel:util/diffuse.glsl"

void flw_materialVertex() {
    flw_vertexNormal = normalize(flw_vertexNormal);

    float diffuseFactor;
    if (flywheel.constantAmbientLight == 1) {
        diffuseFactor = diffuseNether(flw_vertexNormal);
    } else {
        diffuseFactor = diffuse(flw_vertexNormal);
    }
    flw_vertexColor = vec4(flw_vertexColor.rgb * diffuseFactor, flw_vertexColor.a);
}
