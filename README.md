<div align="center">
<img src=".github/logo.png" alt="Logo by jnix" width="250">
<h1>Flywheel</h1>
<h6>Reinventing the wheel so you don't have to.</h6>
<a href='https://ci.tterrag.com/job/Flywheel/job/1.20.1/'><img src='https://ci.tterrag.com/job/Flywheel/job/1.20.1/badge/icon' alt="Jenkins"></a>
<a href="/LICENSE.md"><img src="https://img.shields.io/github/license/Engine-Room/Flywheel?style=flat&color=900c3f" alt="License"></a>
<br>
<a href="https://discord.gg/xjD59ThnXy"><img src="https://img.shields.io/discord/841464837406195712?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/flywheel"><img src="http://cf.way2muchnoise.eu/486392.svg" alt="Curseforge Downloads"></a>
<a href="https://modrinth.com/mod/flywheel"><img src="https://img.shields.io/modrinth/dt/flywheel?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c" alt="Modrinth"></a>
<br>
</div>

### About

The goal of this project is to provide tools for mod developers so they no longer have to worry about performance, or
limitations of Minecraft's archaic rendering engine. That said, this is primarily an outlet for me to have fun with
graphics programming.

### Instancing

Flywheel provides an alternate, unified path for entity and block entity rendering that takes advantage of GPU
instancing. Flywheel gives the developer the flexibility to define their instance formats and write custom shaders
to ingest that data.

To accommodate the developer and leave more in the hands of the engine, Flywheel provides a custom shader loading and
templating system to hide the details of the CPU/GPU interface.

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
    compileOnly fg.deobf("dev.engine_room.flywheel:flywheel-neoforge-api-${minecraft_version}:${flywheel_version}")
    runtimeOnly fg.deobf("dev.engine_room.flywheel:flywheel-neoforge-${minecraft_version}:${flywheel_version}")
}
```
`${flywheel_version}` gets replaced by the version of Flywheel you want to use, eg. `1.0.0-beta`

`${minecraft_version}` gets replaced by the version of Minecraft you're on, eg. `1.20.1`

For a list of available Flywheel versions, you can check [the maven](https://maven.tterrag.com/com/jozufozu/flywheel/Flywheel-Forge/).

If you aren't using mixed mappings (or just want to be safe), add the following properties to your run configurations:
```groovy
property 'mixin.env.remapRefMap', 'true'
property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
```
This ensures that Flywheel's mixins get properly loaded in your dev env.
