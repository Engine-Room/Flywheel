<div align="center">
	<img src="https://i.imgur.com/yVFgPpr.png" alt="Logo by @voxel_dani on Twitter" width="250">
	<h1>Flywheel</h1>
	<h6>A modern engine for modded Minecraft.</h6>
	<a href='https://ci.tterrag.com/job/Flywheel/job/Fabric/job/1.18/'><img src='https://ci.tterrag.com/job/Flywheel/job/Fabric/job/1.18/badge/icon' alt="Jenkins"></a>
	<a href="https://github.com/Jozufozu/Flywheel/blob/HEAD/LICENSE.md"><img src="https://img.shields.io/github/license/Jozufozu/Flywheel?style=flat&color=900c3f" alt="License"></a>
	<a href="https://discord.gg/xjD59ThnXy"><img src="https://img.shields.io/discord/841464837406195712?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
	<a href="https://www.curseforge.com/minecraft/mc-mods/flywheel"><img src="http://cf.way2muchnoise.eu/486392.svg" alt="Curseforge Downloads"></a>
	<a href="https://modrinth.com/mod/flywheel"><img src="https://img.shields.io/modrinth/dt/flywheel?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
	<br></br>
</div>

### About

The goal of this project is to provide tools for mod developers so they no longer have to worry about performance, or
limitations of Minecraft's archaic rendering engine. That said, this is primarily an outlet for me to have fun with
graphics programming.

### Instancing

Flywheel provides an alternate, unified path for entity and block entity rendering that takes advantage of GPU
instancing. In doing so, Flywheel gives the developer the flexibility to define their own vertex and instance formats,
and write custom shaders to ingest that data.

### Shaders

To accommodate the developer and leave more in the hands of the engine, Flywheel provides a custom shader loading and
templating system to hide the details of the CPU/GPU interface. This system is a work in progress. There will be
breaking changes, and I make no guarantees of backwards compatibility.

### Plans

- Vanilla performance improvements
- Compute shader particles
- Deferred rendering
- Different renderers for differently aged hardware

### Getting Started (For Developers)

Add the following repo and dependency to your `build.gradle`:

```groovy
repositories {
    maven {
        name "tterrag maven"
        url "https://maven.tterrag.com/"
    }
}

dependencies {
    modImplementation "com.jozufozu.flywheel:flywheel-fabric-${flywheel_minecraft_version}:${flywheel_version}"
}
```
`${flywheel_version}` gets replaced by the version of Flywheel you want to use, eg. `0.6.9`.
`${flywheel_minecraft_version}` gets replaced by the version of Minecraft, eg. `1.18.2`.

For a list of available Flywheel versions, you can check [the maven](https://maven.tterrag.com/com/jozufozu/flywheel/).
