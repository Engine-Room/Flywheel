#use "flywheel:api/fragment.glsl"
#use "flywheel:context/world.frag"
#use "flywheel:material/default.frag"

void main() {
    flw_initFragment();
    flw_materialFragment();
    flw_contextFragment();
}
