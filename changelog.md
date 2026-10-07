## Changelog
- Added support for ARM CPUs for Windows and Linux. ARM CPUs on MacOS were already supported.
- Renamed seed resolution methods from PascalCase to snake_case. The latter has better command suggestions.
- Changed default seed resolution order. The default order is now: `command_source -> saved_seeds_config -> online_database -> seed_config`. Previously the seed config took precedence over the saved seeds config and the online database. For most players the saved seed config is most useful, and the seed config should mostly be used for temporary stuff.

## Mod compatibility
|      | Mod JAR | Biomes | Structures | Loot | Ores | Slime chunks |
|:----:|:-------:|:------:|:----------:|:----:|:----:|:------------:|
| 26.3 |   ✔️    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
| 26.2 |   ❌    |   ✔️   |     ✔️     |  ✔️  |  ✔️  |      ✔️      |
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
