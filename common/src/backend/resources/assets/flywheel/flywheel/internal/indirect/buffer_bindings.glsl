// FIXME: minimum required SSBO bindings in OpenGL is 8, but we use 9.
//  A few of these could be combined.

// Per culling group
#define _FLW_LAST_FRAME_VISIBILITY_BUFFER_BINDING 0// cull1, cull2
#define _FLW_PAGE_FRAME_DESCRIPTOR_BUFFER_BINDING 1// cull1, cull2
#define _FLW_INSTANCE_BUFFER_BINDING 2// cull1, cull2, draw
#define _FLW_DRAW_INSTANCE_INDEX_BUFFER_BINDING 3// cull1, cull2, draw
#define _FLW_MODEL_BUFFER_BINDING 4// cull1, cull2, apply
#define _FLW_DRAW_BUFFER_BINDING 5// apply, draw


// Global to the engine
#define _FLW_LIGHT_LUT_BUFFER_BINDING 6
#define _FLW_LIGHT_SECTIONS_BUFFER_BINDING 7

#define _FLW_MATRIX_BUFFER_BINDING 8
