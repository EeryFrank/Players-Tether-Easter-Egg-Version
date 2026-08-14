# Player's Tether_Easter Egg Version

![Player's Tether_Easter Egg Version icon](docs/assets/players_tether_icon.png)

A Minecraft 1.21.1 NeoForge mod that lets players leash other players with a normal lead. It includes directional server rules and a six-layer taming easter egg.

Author: **QiZhang**

License: **MIT**

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.244 to 21.1.x
- Java 21
- Install the same JAR on the dedicated server and every client

## Player controls

- Hold a vanilla lead and right-click another player to attach it.
- The holder can sneak-right-click their own tethered target to release it.
- Survival mode consumes a lead and follows vanilla lead-drop behavior.
- Creative mode neither consumes nor creates a free lead.
- Death, logout, dimension changes, invalid holders, excessive distance, or a changed server rule release the tether.
- Cyclic player-to-player tether chains are rejected.

## Taming easter egg

Continuous tethering adds six levels of the **Tamed** effect using decreasing intervals:

| New level | Required continuous time | Total time |
|---:|---:|---:|
| 1 | 30 s | 30 s |
| 2 | 25 s | 55 s |
| 3 | 20 s | 75 s |
| 4 | 15 s | 90 s |
| 5 | 10 s | 100 s |
| 6 | 5 s | 105 s |

- While tethered, the effect duration is refreshed.
- After release, levels 1-6 remain for approximately 10/20/30/40/50/60 seconds.
- At level 3, the player's rendered model becomes a vanilla tamed wolf. This is visual only; player data, inventory, hitbox, and permissions remain unchanged.
- At level 6, heart particles continuously appear around the wolf until the level-6 effect expires.

## Server rules

The default policy allows every player to tether every other player. Pair rules are directional: `holder -> target`.

Only a real permission-level-4 player or the direct local server console can change rules. RCON, command blocks, functions, wrapped `/execute` sources, and permission levels 0-3 are rejected.

Both `/qzleash` and `/玩家拴绳` are accepted as root commands:

```text
/qzleash status
/qzleash default allow
/qzleash default deny
/qzleash allow <holder> <target>
/qzleash deny <holder> <target>
/qzleash clear <holder> <target>
/qzleash check <holder> <target>
/qzleash list
/qzleash reload
/qzleash release <target>
/qzleash release all
```

Rules are stored per world in:

```text
<world>/serverconfig/qizhang-player-leash.properties
```

## Building

Use the included Gradle wrapper and Java 21:

```powershell
.\gradlew.bat clean build
```

The verification lifecycle also runs a deterministic rule-store and taming-schedule self-test. The built JAR is written to `build/libs/`.

## Dependencies and code relationships

The complete external dependency graph, server/client boundary, and internal component relationship diagram are documented in [Dependencies and architecture](docs/DEPENDENCIES_AND_ARCHITECTURE.md).

## Validation status

Automated validation covers compilation, resource/JAR inspection, the rule-store and timing schedule, full startup and clean shutdown on NeoForge 21.1.244, direct-console rule commands, and RCON denial.

The final player-to-player interaction feel, lead rendering, wolf replacement, and heart appearance still require two real clients for visual/gameplay acceptance on a new environment.

## 中文简介

该模组允许玩家使用原版拴绳拴住其他玩家。默认全员可用，权限 4 管理员或本地控制台可以设置定向允许/禁止规则。连续被拴住 105 秒会叠满 6 层“驯服”：第 3 层起显示为原版狼，第 6 层持续冒爱心。服务端与所有客户端都必须安装。

完整中文安装和管理说明见 [docs/README_zh_CN.md](docs/README_zh_CN.md)。
