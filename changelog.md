## Changelog
- Added support for Nether Fossils. In the seed map Nether Fossils with a Dried Ghast have a custom icon. Since Dried Ghast generation depends on the terrain, and SeedMapper's Nether terrain generation is not 100% accurate, this may not be always accurate.
- Added the `minY` and `maxY` optional parameters to the `/sm:highlight terrain` command. If used only the terrain between those values will be highlighted.
- Added support for surface highlighting (`/sm:highlight surface`). This will only highlight the upper face of the topmost surface block. Note that like terrain highlighting, this command is hidden behind the `DevMode` config.
- Added support for End terrain highlighting.

## Mod compatibility
|      | Mod JAR | Biomes | Structures | Loot | Ores | Slime chunks |
|:----:|:-------:|:------:|:----------:|:----:|:----:|:------------:|
| 26.2 |   ✔️    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 26.1 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.21 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.20 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.19 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.18 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.17 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.16 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.15 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.14 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.13 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 1.12 |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.11 |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.10 |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.9  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.8  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.7  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.6  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.5  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.4  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.3  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.2  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.1  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
| 1.0  |   ❌    |   ✔️   |     ✔️     |  ❌  |  ❌  |      ✔️      |
