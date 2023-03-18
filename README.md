Always use the latest (stable) version!
# SeedMapper
In-game Minecraft Fabric mod that allows you to do various things with the world seed. For reference, have a look at the 
[features](#features) this mod has. Keep in mind though, this mod requires you to have access to the seed. If the seed 
is not known, you could crack it using [SeedCrackerX](https://github.com/19MisterX98/SeedcrackerX/) by 19MisterX98. For 
questions and support please head to my [Discord](https://discord.xpple.dev/).

## Disclaimer
This mod does not yet support terrain generation of Minecraft versions above 1.17.1 and all versions below 1.10.2. Support for these versions will 
be added later. A complete compatibility chart can be seen in the release description. Note that the latest release of SeedMapper always 
remains compatible with previously supported Minecraft versions.

## Features
Before using any of these commands, make sure the seed has been configured using `/cconfig seedmapper Seed set <seed>`.

### SeedOverlay
Usage: `/sm:seedoverlay`

Shows an overlay of blocks that don't match the seed's default terrain. Based on the biome, certain blocks are 
"ungenerated". For example, sand will match in a desert, but not in a forest. The combination of terrain comparison and 
ungeneration makes for a great overlay.

### TerrainVersion
Usage: `/sm:terrainversion`

Determines the Minecraft version the terrain has most likely been generated in. This command internally used the 
SeedOverlay principle.

### Locate
Usage: `/sm:locate (biome <biome>)|(feature (structure <structure>)|(slimechunk))|(loot <amount> <item> [with|without] [<enchantment>] [<level>|*])`

Locates a given feature, biome or loot item closest to the player. Depending on the rarity of the biome or loot item, 
this process can take at most 30 seconds.

### Highlight
Usage: `/sm:highlight block <block> [<range>]`

Highlights a given block in a radius. This highlight uses the seed to determine the locations of these blocks, 
which means very few locations may not be valid (especially around caves).

### Source
Usage: `/sm:source (run)|(as <entity>)|(positioned <position>)|(rotated <rotation>)|(in <dimension>)|(versioned <version>)|(seeded <seed>)`

Executes a given command from a modified source. For example, modifying the source's position will execute the command 
as if from that position.

## Installation
1. Install the [Fabric Loader](https://fabricmc.net/use/)
2. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric/) and move it to your mods folder
   - Linux/Windows: `.minecraft/mods`
   - Mac: `minecraft/mods`
3. Download SeedMapper from the [releases page](https://modrinth.com/mod/seedmapper/versions/) and move it to your mods folder

## Post Scriptum
When [QuiltMC](https://quiltmc.org/) officially releases this mod will transfer to Quilt, whereby it has to send the list 
of mods the client is using to the server.
