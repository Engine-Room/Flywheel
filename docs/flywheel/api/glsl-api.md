

# GLSL API

Flywheel backends expect user-authored shaders to follow a specific format.

## Prelude

The Prelude is included by default for all shaders compiled by Flywheel backends.

Some functions/variables are only available for specific shader stages.

::: code-group

<<< stage/common.glsl
<<< stage/material.glsl
<<< stage/vertex.glsl
<<< stage/fragment.glsl

:::

### Uniforms

In addition to the stage-specific variables above, every stage has access to the following uniforms.
All uniforms are included in the prelude and do not have to be manually included.

::: code-group

<<< uniforms/fog.glsl
<<< uniforms/frame.glsl
<<< uniforms/level.glsl
<<< uniforms/options.glsl
<<< uniforms/player.glsl

:::
