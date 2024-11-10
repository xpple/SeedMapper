Always use the latest (stable) version!
# SeedMapper
In-game Minecraft Fabric mod that allows you to do various things with the world seed. For reference, have a look at the 
[features](#features) this mod has. Keep in mind though, this mod requires you to have access to the seed. If the seed 
is not known, you could crack it using [SeedCrackerX](https://github.com/19MisterX98/SeedcrackerX/) by 19MisterX98. For 
questions and support please head to my [Discord](https://discord.xpple.dev/).

## Installation
1. Install the [Fabric Loader](https://fabricmc.net/use/)
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric/) and move it to your mods folder
   - Linux/Windows: `.minecraft/mods`
   - Mac: `minecraft/mods`
3. Download SeedMapper from the [releases page](https://modrinth.com/mod/seedmapper/versions/) and move it to your mods folder

## IMPORTANT
You need to have Java 23 installed to use this mod. I recommend to get Java 23 from [adoptium.net](https://adoptium.net/temurin/releases/?version=23). Next, configure your Minecraft launcher to use this release of Java.
- Vanilla launcher: Go to `Installations` -> `Edit` -> `More options` -> `Java executable`.
- MultiMC: Go to `Edit Instance` -> `Settings` -> `Java` -> `Java Installation`.
- PrismLauncher: Go to `Settings` -> `Java` -> `Java Runtime` -> `Auto-Detect...`.

If you run into issues, contact your launcher's support.

## Features
Before using any of these commands, make sure the seed has been configured using `/cconfig seedmapper Seed set <seed>`.

### Biome locating
Usage: `/sm:locate biome <biome>`

Locates a given biome closest to the player. All biomes in all dimensions are supported.

### Structure locating
Usage: `/sm:locate feature structure <structure>`

Locates a given structure closest to the player. All structures in all dimensions are supported. However, due to limitations in the underlying library, some structures (in particular desert pyramids, jungle temples and woodland mansions) may result in occasional false positives.

### Slime chunk locating
Usage: `/sm:locate feature slimechunk`

Locates a slime chunk closest to the player. This will always be accurate.

### Source mutation
Usage: `/sm:source (run)|(as <entity>)|(positioned <position>)|(rotated <rotation>)|(in <dimension>)|(versioned <version>)|(seeded <seed>)`

Executes a given command from a modified source. For example, modifying the source's position will execute the command 
as if you were in that position. This command is really powerful, use it!

## Building the mod locally
This mod internally uses (a fork of) the C library [cubiomes](https://github.com/Cubitect/cubiomes) by Cubitect. Java bindings for this library were created with [jextract](https://github.com/openjdk/jextract). The bindings use the [Foreign Function & Memory API](https://openjdk.org/jeps/454) from [Project Panama](https://openjdk.org/projects/panama/).

To build the mod from scratch, do the following:
1. Install [LLVM 13.0.0](https://github.com/llvm/llvm-project/releases/tag/llvmorg-13.0.0)
2. Clone `jextract` and build it:
   ```shell
   ./gradlew -Pjdk22_home=<jdk22_home_dir> -Pllvm_home=<libclang_dir> clean verify
   ```
   You will find the tool in the `build/jextract/bin/` folder. You must use Java 22 to build `jextract`!
3. Compile cubiomes to a shared library (the following is for Windows):
   ```
   gcc -shared -o src/main/resources/libcubiomes.dll src/main/c/noise.c src/main/c/biomes.c src/main/c/layers.c src/main/c/biomenoise.c src/main/c/generator.c src/main/c/finders.c src/main/c/util.c src/main/c/quadbase.c -O3
   ```
4. Run the following command:
   ```shell
   jextract --include-dir src/main/c --output src/main/java --target-package com.github.cubiomes --library src/main/resources/libcubiomes --header-class-name Cubiomes --use-system-load-library src/main/c/tables/btree18.h tables/btree19.h tables/btree20.h tables/btree192.h tables/btree21wd.h biomenoise.h biomes.h finders.h generator.h layers.h noise.h quadbase.h rng.h util.h
   ```
5. Delete any library loads in `Cubiomes_1.java`
