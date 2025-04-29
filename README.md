Always use the latest (stable) version!
# SeedMapper
In-game Minecraft Fabric mod that allows you to do various things with the world seed. For reference, have a look at the 
[features](#features) this mod has. Keep in mind though, this mod requires you to have access to the seed. If the seed 
is not known, you could crack it using [SeedCrackerX](https://github.com/19MisterX98/SeedcrackerX/) by 19MisterX98. For 
questions and support please head to my [Discord](https://discord.xpple.dev/).

## Installation
1. Install the [Fabric Loader](https://fabricmc.net/use/).
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric/) and move it to your mods folder:
   - Linux/Windows: `.minecraft/mods`.
   - Mac: `minecraft/mods`.
3. Download SeedMapper from the [releases page](https://modrinth.com/mod/seedmapper/versions/) and move it to your mods folder.

## IMPORTANT
You need to have Java 23 installed to use this mod. I recommend to get Java 23 from [adoptium.net](https://adoptium.net/temurin/releases/?version=23). Next, configure your Minecraft launcher to use this release of Java.
- Vanilla launcher: Go to `Installations` -> `Edit` -> `More options` -> `Java executable`.
- MultiMC: Go to `Edit Instance` -> `Settings` -> `Java` -> `Java Installation`.
- PrismLauncher: Go to `Settings` -> `Java` -> `Java Runtime` -> `Auto-Detect...`.
  - Do not forget to enable "Skip Java compatibility checks".

If you run into issues, contact your launcher's support.

## Features
Before using any of these commands, make sure the seed has been configured using `/cconfig seedmapper Seed set <seed>`.

### Biome locating
Usage: `/sm:locate biome <biome>`.

Locates a given biome closest to the player. All biomes in all dimensions are supported.

### Structure locating
Usage: `/sm:locate feature structure <structure>[<pieces>]{<variants>}`.

Locates a given structure closest to the player. All structures in all dimensions are supported. However, due to limitations in the underlying library, some structures (in particular desert pyramids, jungle temples and woodland mansions) may result in occasional false positives. For more advanced querying you can also use piece and variant data to further restrict the search. For example, the following command will search for end cities with ships: `/sm:locate feature structure end_city[end_ship]`.

### Ore highlighting
Usage: `/sm:highlight block <block>`.

Highlights the specified block in the world. All versions from 1.13 onwards are supported. Due to high dependence on the [`OCEAN_FLOOR_WG`](https://minecraft.wiki/w/Heightmap#OCEAN_FLOOR_WG) heightmap, coal, copper and emerald ore locations may be off.

### Slime chunk locating
Usage: `/sm:locate feature slimechunk`.

Locates a slime chunk closest to the player. This will always be accurate.

### Source mutation
Usage: `/sm:source (run)|(as <entity>)|(positioned <position>)|(rotated <rotation>)|(in <dimension>)|(versioned <version>)|(seeded <seed>)`.

Executes a given command from a modified source. For example, modifying the source's position will execute the command 
as if you were in that position. This command is really powerful, use it!

## Building from source
This mod internally uses (a fork of) the C library [cubiomes](https://github.com/Cubitect/cubiomes) by Cubitect. Java bindings for this library were created with (also a fork of) [jextract](https://github.com/openjdk/jextract). The bindings use the [Foreign Function & Memory API](https://openjdk.org/jeps/454) from [Project Panama](https://openjdk.org/projects/panama/). See [CreateJavaBindingsTask.java](https://github.com/xpple/SeedMapper/blob/master/buildSrc/src/main/java/dev/xpple/seedmapper/buildscript/CreateJavaBindingsTask.java) for the Gradle task that automates this.

To build the mod locally, follow these steps:

1. Compile cubiomes to a shared library. The following is for Windows:
   ```shell
   gcc -shared -o src/main/resources/cubiomes.dll src/main/c/noise.c src/main/c/biomes.c src/main/c/layers.c src/main/c/biomenoise.c src/main/c/generator.c src/main/c/finders.c src/main/c/util.c src/main/c/quadbase.c -O3
   ```
2. Install LLVM (version 13.0.0 is recommended) and set the environment variable `LLVM_HOME` to the directory where LLVM was installed.
3. Compile jextract:
   ```shell
   cd jextract
   ./gradlew --stacktrace -Pjdk_home=$JAVA_HOME -Pllvm_home=$LLVM_HOME clean verify
   ```
4. Build the mod:
   ```shell
   ./gradlew build
   ```
   You should find the Java bindings in `src/main/java/com/github/cubiomes`.
