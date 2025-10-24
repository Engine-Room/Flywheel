

# GLSL API

Flywheel backends expect user-authored shaders to follow a specific format.

## Prelude

The Prelude is included by default for all shaders compiled by Flywheel backends.

Some functions/variables are only available for specific shader stages.

::: code-group

<<< snippets/common.glsl
<<< snippets/material.glsl
<<< snippets/vertex.glsl
<<< snippets/fragment.glsl

:::
