## Changelog
- Implemented vault prediction. Vault loot depends only on the world seed, and follows a predetermined sequence. (The items displayed in the vault when active are purely visual.) As such, given the world seed, you can predict all future vault loot drops. Note that the sequence is server global, so every player uses the same sequence.

  To predict future loot, you must first determine the current offset in the sequence. To do this, execute `/sm:vault detect`, and open a vault. SeedMapper will detect the items ejected from the vault, and use those to compute the offset. The offset (an integer) is outputted, as well as the actual random state of the sequence (a hexadecimal string).

  Next, you can predict the loot. You can use two methods. In both commands, `<ominous>` is either `true` or `false`, depending on whether the vault is an ominous vault.

  1. `/sm:vault predict state <state> <ominous> [<amount>]`. When you enter the random state from the `detect` command, you will see the future loot based on that random state. This command is the fastest, as it reuses the state from the detection phase.
  2. `/sm:vault predict offset <offset> <ominous> [<amount>]`. When you enter the offset from the `detect` command, you will see the future loot based on the provided offset. This command is slower, as it must iterate through every step of the sequence to get to the sequence position specified by the offset. This command is more customisable though, as you can for example specify a lower or higher offset.

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
