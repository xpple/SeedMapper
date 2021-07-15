# SeedMapper
In-game Minecraft mod that allows you to compare natural terrain to your current terrain. For questions please go to my 
[Discord](https://discord.xpple.dev/).

## Disclaimer
This mod does not yet support terrain generation of Minecraft version 1.12.2 and below. Support for these versions will be added later.

## Features
Before using any of these commands, make sure the seed has been configured using `/seedmapper:config seed set <seed>`.
### SeedOverlay
Usage: `/seedmapper:seedoverlay [<version>]`

Shows an overlay of blocks that don't match the seed's default terrain. Based on the biome, certain blocks are 
"ungenerated". For example, sand will match in a desert, but not in a forest. The combination of terrain comparison and 
ungeneration makes for a great overlay.

### TerrainVersion
Usage: `/seedmapper:terrainversion`

Determines the Minecraft version the terrain has most likely been generated in. This command internally used the 
SeedOverlay principle.

### Locate
Usage: `/seedmapper:locate (biome <biome> <version>)|(feature (structure <structure> <version>)|(slimechunk <version>)) `

Locates a given feature or biome closest to the player. Depending on the rarity of the biome, this process can take at 
most 30 seconds.

### Source
Usage: `/seedmapper:source (run)|(as <entity>)|(positioned <position>)|(rotated <rotation>)|(in <dimension>)`

Executes a given command from a modified source. For example, modifying the source's position will execute the command 
as if from that position.

## Installation
1. Install the [Fabric Loader](https://fabricmc.net/use/)
1. Download the [Fabric API](https://minecraft.curseforge.com/projects/fabric/) and move it to your mods folder
   - Linux/Windows: `.minecraft/mods`
   - Mac: `minecraft/mods`
1. Download SeedMapper from the [releases page](https://modrinth.com/mod/seedmapper/versions/) and move it to your mods folder

## Post Scriptum
When [QuiltMC](https://quiltmc.org/) officially releases this mod will transfer to Quilt, whereby it has to send the list 
of mods the client is using to the server.
