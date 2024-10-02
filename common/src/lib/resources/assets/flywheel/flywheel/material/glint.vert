void flw_materialVertex() {
    float p = flw_glintSpeedOption * flw_systemSeconds * 8.;

    flw_vertexTexCoord *= 8.;
    // Rotate by 0.17453292 radians
    flw_vertexTexCoord *= mat2(0.98480775, 0.17364817, -0.17364817, 0.98480775);
    flw_vertexTexCoord += vec2(-p / 110., p / 30.);
}
