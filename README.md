# Player's Tether_Easter Egg Version

![Player's Tether_Easter Egg Version icon](docs/assets/players_tether_icon.png)

A Minecraft 1.20.1/1.21.1 mod that lets players leash other players with a normal lead. Fabric, NeoForge, and Forge builds are published separately. The mod includes directional server rules and a six-layer taming easter egg.

Author: **QiZhang**

License: **MIT**

## Supported targets

| Minecraft | Loader build target | Java | Release status |
| --- | --- | ---: | --- |
| 1.21.1 | Fabric Loader 0.19.3 + Fabric API 0.116.15 | 21 | Supported |
| 1.21.1 | NeoForge 21.1.244 | 21 | Supported |
| 1.21.1 | Forge 52.1.0 | 21 | Supported |
| 1.20.1 | Fabric Loader 0.19.3 + Fabric API 0.92.11 | 17 | Supported |
| 1.20.1 | NeoForge 47.1.106 legacy line | 17 | Experimental |
| 1.20.1 | Forge 47.4.10 | 17 | Supported |

Install the JAR whose filename exactly matches the Minecraft version and loader on the dedicated server and every client. The six JARs are not interchangeable.

NeoForge 1.20.1 was a short-lived Forge-compatible line that is no longer maintained upstream. Its build is provided for legacy installations and must not be treated as modern NeoForge support.

On NeoForge 1.21.1, QiZhang Aquaculture Turtle Companion 1.0.0+ is an optional
server-side compatibility mod. Player's Tether handles player targets while the
companion handles its owned non-player living entities; metadata only provides
deterministic load ordering and does not make either mod mandatory.

## Player controls

- Hold a vanilla lead and right-click another player to attach it.
- The holder can sneak-right-click their own tethered target to release it.
- Survival mode consumes a lead and follows vanilla lead-drop behavior.
- Creative mode neither consumes nor creates a free lead.
- Death, logout, dimension changes, invalid holders, or a changed server rule release the tether.
- A valid player tether remains elastic beyond vanilla's ten-block cutoff instead of snapping solely because of distance.
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

Use the included Gradle wrapper with a Java 21 build JVM. Gradle automatically selects Java 17 for the 1.20.1 compilation and run tasks:

```powershell
.\gradlew.bat --no-daemon --console=plain clean buildAll collectReleaseJars
pwsh -NoProfile -File .\tools\Verify-Release.ps1
```

The verification lifecycle runs deterministic rule-store and taming-schedule self-tests for every target. Six production JARs and `SHA256SUMS.txt` are written to `build/release/`.

To build only one target, for example Fabric 1.20.1:

```powershell
.\gradlew.bat :fabric-1.20.1:build
```

## Dependencies and code relationships

The complete external dependency graph, server/client boundary, and internal component relationship diagram are documented in [Dependencies and architecture](docs/DEPENDENCIES_AND_ARCHITECTURE.md).

## Validation status

Automated validation covers all six compilations, deterministic self-tests, loader metadata, version-specific Mixin/refmap contents, resource-pack formats, Java class versions, and release checksums. Dedicated-server smoke tests reached `Done` and shut down cleanly on all six targets; the 1.20.1 targets also reported Java 17 and accepted the direct-console status command.

`NEEDS_MANUAL_VALIDATION`: two real clients are still required to accept player-to-player synchronization, pulling and long-distance behavior, lead rendering, wolf replacement, and heart appearance. This is especially important for the custom 1.20.1 tether renderer.

## 中文简介

该模组允许玩家使用原版拴绳拴住其他玩家，提供 Minecraft 1.20.1/1.21.1 的 Fabric、NeoForge、Forge 六个独立 JAR。默认全员可用，权限 4 管理员或本地控制台可以设置定向允许/禁止规则。连续被拴住 105 秒会叠满 6 层“驯服”：第 3 层起显示为原版狼，第 6 层持续冒爱心。服务端与所有客户端必须安装同版本、同加载器的 JAR。

完整中文安装和管理说明见 [docs/README_zh_CN.md](docs/README_zh_CN.md)。
