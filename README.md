# Flywheel
A modern engine for modded Minecraft.


The goal of this project is to provide tools for mod developers so they no longer have to worry about performance, or limitations of Minecraft's archaic rendering engine.
That said, this is primarily an outlet for me to have fun with graphics programming.


## Instancing

So far, Flywheel provides an alternate, unified path for entity and tile entity rendering that takes advantage of GPU instancing. In doing so, Flywheel gives the developer the flexibility to define their own vertex and instance formats, and write custom shaders to ingest that data.

### Shader Abstraction
To accomodate the developer and leave more in the hands of the engine, Flywheel provides a custom shader loading and templating system to hide the details of the CPU/GPU interface. This system is a work in progress. There will be breaking changes, and I make no guarantees of backwards compatibility.
