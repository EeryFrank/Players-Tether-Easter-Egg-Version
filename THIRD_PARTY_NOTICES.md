<!-- SPDX-License-Identifier: GPL-3.0-only -->

# Third-party notices

## Gradle Wrapper 8.14.1

The following Gradle-provided/generated files are excluded from the project's GPL scope and retain Apache License 2.0:

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`

- Upstream: [Gradle](https://github.com/gradle/gradle)
- Fixed version: `8.14.1`
- Local terms: [LICENSES/Apache-2.0.txt](LICENSES/Apache-2.0.txt)
- Upstream terms: [Gradle v8.14.1 LICENSE](https://github.com/gradle/gradle/blob/v8.14.1/LICENSE)
- Project use: build bootstrap only; these files are not packaged in production mod JARs

## Historical Minecraft texture correction

Releases through `v1.1.5` included `assets/qizhang_player_leash/textures/mob_effect/tamed.png`, whose bytes were identical to Minecraft 1.21.1's `assets/minecraft/textures/item/bone.png` (`SHA-256 F5F2A37E7466F2983C00550874A2A94D2D006EAE52B0E73ACB722FADEB9BDB3B`). That file was Mojang/Microsoft material, not project-original MIT content. The historical project MIT notice did not relicense it.

Version `1.1.6` removes those bytes and replaces them with independently generated project art recorded under `CC-BY-SA-4.0` in [ASSET_LICENSES.md](ASSET_LICENSES.md). Current production verification rejects the former hash. Minecraft assets and use of Minecraft remain subject to the official [Minecraft EULA](https://www.minecraft.net/eula) and [Usage Guidelines](https://www.minecraft.net/usage-guidelines).

The current project does not vendor Minecraft, loader, Fabric API, Forge, NeoForge, Architectury Loom, or optional companion-mod code. Build dependencies are resolved externally and retain their upstream licenses. Production verification rejects nested dependency JARs.

The MIT text under `LICENSES/MIT.txt` preserves the project's own historical grant and the three current project-icon exceptions; it is not a third-party dependency notice and does not apply to the corrected Minecraft texture above. The preserved `LICENSES/LGPL-3.0-or-later.txt` records the project's later public source grant through baseline `1d11c9bee63f17f51b554b4413c5e82e559f7ffa`; it likewise is historical project licensing, not a dependency notice.

Standard GPL, historical LGPL, CC BY-SA, Apache, and MIT texts are fixed local copies. The project asset LicenseRef is a separate policy text and does not relicense third-party material. New third-party files must be recorded here with exact path, author, upstream URL, fixed version or revision, license, modifications, and packaging scope.
