<div align="center">
<img src="https://i.imgur.com/yVFgPpr.png" alt="Logo by @voxel_dani on Twitter" width="250">
<h1>Flywheel</h1>
<h6>A modern engine for modded Minecraft.</h6>
<a href='https://ci.tterrag.com/job/Flywheel/job/Forge/job/1.16/'><img src='https://ci.tterrag.com/job/Flywheel/job/Forge/job/1.16/badge/icon' alt="Jenkins"></a>
<a href="https://discord.gg/xjD59ThnXy"><img src="https://img.shields.io/discord/841464837406195712?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/flywheel"><img src="http://cf.way2muchnoise.eu/486392.svg" alt="Curseforge Downloads"></a>
<br>
</div>
d
## About
The goal of this project is to provide tools for mod developers so they no longer have to worry about performance, or limitations of Minecraft's archaic rendering engine.
That said, this is primarily an outlet for me to have fun with graphics programming.


### Instancing
Flywheel provides an alternate, unified path for entity and tile entity rendering that takes advantage of GPU instancing. In doing so, Flywheel gives the developer the flexibility to define their own vertex and instance formats, and write custom shaders to ingest that data.


### Shaders
To accomodate the developer and leave more in the hands of the engine, Flywheel provides a custom shader loading and templating system to hide the details of the CPU/GPU interface. This system is a work in progress. There will be breaking changes, and I make no guarantees of backwards compatibility.


### Plans
 - Vanilla performance improvements
 - Compute shader particles
 - Deferred rendering
 - Different renderers for differently aged hardware


### Getting Started (For Developers)

Add the following repo to your `build.gradle`:
```groovy
repositories {
    maven {
        name "tterrag maven"
        url "https://maven.tterrag.com/"
    }
}
```

Then add Flywheel as a dependency:
```groovy
dependencies {
    compile fg.deobf("com.jozufozu.flywheel:Flywheel:1.16-0.0.2.6")
}
```
