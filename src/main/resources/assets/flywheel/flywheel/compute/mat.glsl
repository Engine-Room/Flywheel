/*
This is what generated ubershaders should look like

uint flw_materialVertexID = 0;
uint flw_materialFragmentID = 0;

void flw_materialVertex() {
    switch (flw_materialVertexID) {
    case 0: flw_materialVertex_flywheel_cutout_vert(); break;
    default: break;
    }
}

void flw_materialFragment() {
    switch (flw_materialFragmentID) {
    case 0: flw_materialFragment_flywheel_cutout_frag(); break;
    default: break;
    }
}

bool flw_discardPredicate(vec4 finalColor) {
    switch (flw_materialFragmentID) {
    case 0: return flw_discardPredicate_flywheel_cutout_frag(finalColor);
    default: return false;
    }
}

vec4 flw_fogFilter(vec4 color) {
    switch (flw_materialFragmentID) {
    case 0: return flw_fogFilter_flywheel_cutout_frag(color);
    default: return color;
    }
}


void flw_materialVertex_flywheel_cutout_vert() {
}

void flw_materialFragment_flywheel_cutout_frag() {
}

bool flw_discardPredicate_flywheel_cutout_frag(vec4 finalColor) {
    return finalColor.a < 0.1;
}

vec4 flw_fogFilter_flywheel_cutout_frag(vec4 color) {
    return linear_fog(color, flw_distance, flw_fogRange.x, flw_fogRange.y, flw_fogColor);
}

*/
