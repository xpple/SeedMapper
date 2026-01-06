**Always** use the latest (stable) version of SeedMapper! If you want to play on an older version of Minecraft, use [ViaFabricPlus](https://modrinth.com/mod/viafabricplus). This mod allows you to use the latest features of SeedMapper, while still being able to play on older Minecraft versions.

# SeedMapper
In-game Minecraft Fabric mod that allows you to do various things with the world seed. For reference, have a look at the [features](#features) this mod has. Keep in mind though, this mod requires you to have access to the seed. If the seed is not known, you could crack it using [SeedCrackerX](https://github.com/19MisterX98/SeedcrackerX/) by 19MisterX98. For questions and support please head to my [Discord](https://discord.xpple.dev/).

## Installation
1. Install the [Fabric Loader](https://fabricmc.net/use/).
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric/) and move it to your mods folder:
   - Linux/Windows: `.minecraft/mods`.
   - Mac: `minecraft/mods`.
3. Download SeedMapper from the [releases page](https://modrinth.com/mod/seedmapper/versions/) and move it to your mods folder.

## IMPORTANT
You need to have at least Java 23 installed to use this mod. I recommend to get Java 23 (or higher) from [adoptium.net](https://adoptium.net/temurin/releases/?version=23). Next, configure your Minecraft launcher to use this release of Java.

- Vanilla launcher: Go to `Installations` -> `Edit` -> `More options` -> `Java executable`.
- MultiMC: Go to `Edit Instance` -> `Settings` -> `Java` -> `Java Installation`.
- PrismLauncher: Go to `Settings` -> `Java` -> `Java Runtime` -> `Auto-Detect...`.
- Modrinth App: Go to `Instance settings` -> `Java and memory` -> `Custom Java installation` -> `Browse`

Sometimes it may be necessary to click the option for skipping the Java compatibility check.

If you are on Windows, make sure to select `javaw.exe`, not `java.exe`.

If you run into issues, contact your launcher's support.

## Features
Before using any of these commands, make sure the seed has been configured using `/sm:config Seed set <seed>`.

### Seed map
Usage: `/sm:seedmap`.

Opens an explorable seed map based on the configured seed. You can move the map by dragging the mouse, and zoom in or out by using the scroll wheel. You can toggle what features are visible by clicking the feature toggles at the top of the screen. This command is especially useful in combination with the `/sm:source` command!

### Seed minimap
Usage: `/sm:minimap [show|hide]`.

Displays a minimap based on configured seed in the heads-up display (HUD). Depending on whether the `RotateMinimap` config is enabled, the minimap will rotate with the player. The minimap displays the same features as the seed map (see above). Changing dimension will automatically update the minimap. The configs `MinimapOffsetX`, `MinimapOffsetY`, `MinimapWidth` and `MinimapHeight` can be used to further customise the minimap.

### Biome locating
Usage: `/sm:locate biome <biome>`.

Locates a given biome closest to the player. All biomes in all dimensions are supported.

### Structure locating
Usage: `/sm:locate feature <structure>[<pieces>]{<variants>}`.

Locates a given structure closest to the player. All structures in all dimensions are supported. However, due to limitations in the underlying library, some structures (in particular desert pyramids, jungle temples and woodland mansions) may result in occasional false positives. For more advanced querying you can also use piece and variant data to further restrict the search. For example, the following command will search for end cities with ships: `/sm:locate feature structure end_city[end_ship]`.

### Ore vein locating
Usage: `/sm:locate orevein (copper|iron)`.

Locates an [ore vein](https://minecraft.wiki/w/Ore_vein) closest to the player. The coordinates of the first ore vein block found will be returned. After this, you can use [`/sm:highlight orevein [<chunks>]`](#ore-vein-highlighting) to highlight the other ores.

### Loot locating
Usage: `/sm:locate loot <amount> <item> [<enchantment conditions>]`.

Locates chest loot closest to the player. All versions from 1.13 onwards are supported. SeedMapper will search through the chest loot of structures to find loot that matches the item and enchantment conditions. Note that queries for unobtainable loot and illegal enchantment combinations are not prevented by the command. If a search is taking too long, you should probably cancel it using `/sm:stoptask`.

### Ore highlighting
Usage: `/sm:highlight block <block> [<chunks>]`.

Highlights the specified block in the world. All versions from 1.13 onwards are supported. Due to high dependence on the [`OCEAN_FLOOR_WG`](https://minecraft.wiki/w/Heightmap#OCEAN_FLOOR_WG) heightmap, coal, copper and emerald ore locations may be off.

### Ore vein highlighting
Usage: `/sm:highlight orevein [<chunks>]`.

Highlights ore veins in the world. Raw ore blocks that generate as part of the ore vein are highlighted distinctly. Filler blocks are ignored.

### Slime chunk locating
Usage: `/sm:locate slimechunk`.

Locates a slime chunk closest to the player. This will always be accurate.

### Source mutation
Usage: `/sm:source (run)|(as <entity>)|(positioned <position>)|(rotated <rotation>)|(in <dimension>)|(versioned <version>)|(seeded <seed>)`.

Executes a given command from a modified source. For example, modifying the source's position will execute the command as if you were in that position. This command is really powerful, use it!

### Baritone integration
If [Meteor's version of Baritone](https://maven.meteordev.org/#/snapshots/meteordevelopment/baritone) is present, the `AutoMine` config will be visible. When set to true, certain blocks highlighted by `/sm:highlight` will be automatically mined. You can stop Baritone by setting the config back to false (`#stop` will not work). Make sure `#allowBreak` is set to true in Baritone's configs.

## Building from source
This mod internally uses (a fork of) the C library [cubiomes](https://github.com/Cubitect/cubiomes) by Cubitect. Java bindings for this library were created with (also a fork of) [jextract](https://github.com/openjdk/jextract). The bindings use the [Foreign Function & Memory API](https://openjdk.org/jeps/454) from [Project Panama](https://openjdk.org/projects/panama/). See [CreateJavaBindingsTask.java](https://github.com/xpple/SeedMapper/blob/master/buildSrc/src/main/java/dev/xpple/seedmapper/buildscript/CreateJavaBindingsTask.java) for the Gradle task that automates this.

To build the mod locally, follow these steps:

1. Clone the repository:
   ```shell
   git clone --recurse-submodules https://github.com/xpple/SeedMapper
   cd SeedMapper
   ```
2. Compile cubiomes to a shared library. MSVC cannot be used to build the project! The following is for Windows:
   ```shell
   cd src/main/c/cubiomes
   cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
   cmake --build build --config Release
   cp build/cubiomes.dll ../../resources/cubiomes.dll
   cd ../../../../
   ```
3. Install LLVM (version 13.0.0 is recommended) and set the environment variable `LLVM_HOME` to the directory where LLVM was installed.
4. Compile jextract. Again, the following is for Windows:
   ```shell
   cd jextract
   ./gradlew --stacktrace -Pjdk_home="$env:JAVA_HOME" -Pllvm_home="$env:LLVM_HOME" clean verify
   cd ../
   ```
5. Build the mod:
   ```shell
   ./gradlew build
   ```
   You should find the Java bindings in `src/main/java/com/github/cubiomes`.

Lastly, you can also consult the [GitHub Actions workflow file](https://github.com/xpple/SeedMapper/blob/master/.github/workflows/build.yml), which contains complete build instructions for each major OS.
