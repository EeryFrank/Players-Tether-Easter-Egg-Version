<!-- SPDX-License-Identifier: LGPL-3.0-or-later -->

# Player's Tether license policy

This file defines the license boundary for each kind of content in this repository. A more specific per-file notice takes precedence.

## License transition

Tags `v1.0.0` through `v1.1.5`, their release artifacts, and repository content obtained before this policy was introduced were published with an MIT License notice. Those grants remain valid for content the project had authority to license and are not revoked. They do not relicense third-party material; the historical Minecraft-texture correction is recorded in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). A copy of the historical project license is preserved at [LICENSES/MIT.txt](LICENSES/MIT.txt).

Beginning with the commit that introduces this policy, the current and future versions use the file-scoped licenses below.

## Project code and functional content

Original Java source, tests, Gradle files, PowerShell tools, CI configuration, loader metadata, functional JSON/TOML, localization, documentation, and other software-oriented content use `LGPL-3.0-or-later`. The complete terms are in [LICENSE](LICENSE).

A contribution to this scope is provided under `LGPL-3.0-or-later` unless the file carries a more specific notice.

## Existing MIT image exceptions

The three project-icon PNG files listed in [ASSET_LICENSES.md](ASSET_LICENSES.md) already appeared in the MIT-licensed releases. They remain available under the preserved MIT License and are not reclassified as CC content by this transition.

Current production JARs contain LGPL code and functional content, an MIT project icon, and a CC BY-SA effect icon. Their package metadata uses `LGPL-3.0-or-later AND MIT AND CC-BY-SA-4.0`; this policy and the asset inventory supply the per-file interpretation.

## Future original creative assets

New, project-original creative assets added after this policy use `CC-BY-SA-4.0` when they are explicitly recorded as such in `ASSET_LICENSES.md`. Eligible content includes original textures, model/animation art, fonts, sound, music, and source art under `art/**` or a loader/common resource tree such as `common/src/main/resources/assets/qizhang_player_leash/**`, with creative formats such as PNG, SVG, BBMODEL, BLEND, GLTF, GLB, OGG, WAV, FLAC, TTF, or OTF. The complete terms are in [LICENSES/CC-BY-SA-4.0.txt](LICENSES/CC-BY-SA-4.0.txt).

Configuration and program files do not become CC content merely because they share an asset directory. Shader programs, `.mcmeta`, `sounds.json`, font-provider JSON, language JSON, and other functional data remain in the LGPL scope unless a specific notice says otherwise.

Suggested attribution for future CC assets:

```text
Player's Tether_Easter Egg Version (QiZhang and contributors)
https://github.com/EeryFrank/Players-Tether-Easter-Egg-Version
```

## Name, icon, and project identity

No license in this repository grants trademark rights in the project name, its official icon, or other source identifiers, and no use may imply unauthorised endorsement. This does not restrict accurate attribution, compatibility statements, or factual reference to the project.

Paths matching `**/assets/branding/**`, and files explicitly designated as an official logo or icon, are not automatically placed under CC. Existing icon files remain distributable under their recorded MIT terms; a replacement branding asset must include explicit terms that still permit unmodified official packages and modpacks to be redistributed.

## Third-party content

Third-party code, tools, dependencies, assets, and quoted material retain their own licenses. They are not relicensed by being referenced from this repository. Repository-held third-party files are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Standard license-text copies provide their respective legal terms and are not themselves reclassified by their repository path. The project-authored text in this policy, `ASSET_LICENSES.md`, `THIRD_PARTY_NOTICES.md`, and `CONTRIBUTING.md` belongs to the LGPL documentation scope above.
