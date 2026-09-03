## Changelog
- Added Buried Treasure cluster finder (`/sm:find cluster buried_treasure`). Buried Treasures have a 1% chance of generating in each chunk (if the biomes are right). A seedfinding task might be to find the largest cluster of Buried Treasures, where a cluster is a formation of Buried Treasures that are connected either horizontally, vertically, or diagonally. It turns out that the largest cluster is 7 Buried Treasures. A cluster of 8 Buried Treasures does not exist.

  SeedMapper uses a list of precomputed seeds that have a cluster of 7 Buried Treasures at spawn. Using some maths, you can transform these seeds into your configured seed, with the effect being that the coordinates change. Generally, results will be very far from spawn. But even if it's hard to travel there, it's cool to know that a maximal cluster exists in your seed.

  I plan to add more commands with this seedfinding theme. If you have any ideas, make sure to let me know!

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
