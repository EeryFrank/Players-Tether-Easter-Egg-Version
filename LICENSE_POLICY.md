<!-- SPDX-License-Identifier: GPL-3.0-only -->
<!-- Asset-Default-License: LicenseRef-EeryFrank-Assets-Permission-Required -->
<!-- Historical-Grants: MIT-and-LGPL-not-revoked -->

# Player's Tether license policy

This file defines the current repository's file-by-file license boundary. A more specific file notice or an explicit row in [ASSET_LICENSES.md](ASSET_LICENSES.md) takes precedence. Nothing here relicenses third-party material or withdraws a license already granted for an earlier copy.

## Current and future project source

Beginning with the commit that introduces this policy, project-authored Java source, tests, Gradle files, PowerShell tools, CI configuration, loader metadata, functional JSON/TOML, localization, documentation, and other software-oriented or functional material are licensed under `GPL-3.0-only`, unless a file says otherwise. The unmodified official GPLv3 text is in [LICENSE](LICENSE).

Accepted contributions to this scope must be owned by the contributor or submitted with authority to license them under `GPL-3.0-only`. This policy does not unilaterally relicense material owned by an outside collaborator.

## Historical MIT and LGPL grants

Tags `v1.0.0` through `v1.1.5`, their release artifacts, and the project-owned content in those copies were distributed with an MIT notice. The preserved text is at [LICENSES/MIT.txt](LICENSES/MIT.txt). That notice never relicensed the historical Mojang texture identified in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

The later public baseline `1d11c9bee63f17f51b554b4413c5e82e559f7ffa` offered project-authored source and functional material under `LGPL-3.0-or-later`. Its exact text is preserved at [LICENSES/LGPL-3.0-or-later.txt](LICENSES/LGPL-3.0-or-later.txt).

Those MIT and LGPL grants remain valid for the copies and material to which they applied. The GPL transition is prospective: it does not rewrite historical tags or releases, replace their license files, or withdraw permissions already received.

## Existing asset exceptions and current package expression

The three project-icon PNG files listed in [ASSET_LICENSES.md](ASSET_LICENSES.md) retain their historical MIT grants, fixed hashes, provenance record, and separate trademark boundary. They are not relicensed under GPL or the new asset LicenseRef.

The recorded source and final tamed-effect images retain their existing `CC-BY-SA-4.0` grant. That grant is not withdrawn and is not changed into the new default. The complete terms remain at [LICENSES/CC-BY-SA-4.0.txt](LICENSES/CC-BY-SA-4.0.txt).

Current production JARs therefore contain GPL source/functional material, an MIT project icon, and a CC BY-SA effect icon. Loader metadata uses the SPDX expression `GPL-3.0-only AND MIT AND CC-BY-SA-4.0`; the asset inventory supplies the per-file interpretation.

## Future visual, audio, and branding assets

No directory or filename automatically assigns an asset license. A future project-owned texture, model, animation, illustration, font, sound, music, logo, icon, or other branding asset must first pass a provenance, ownership, source-input, tool-terms, and written-authority review and then be explicitly added to `ASSET_LICENSES.md` with its creator, source, modifications, fixed SHA-256, and applicable terms.

Unless that inventory records another expressly authorized license, the asset uses `LicenseRef-EeryFrank-Assets-Permission-Required`, whose complete terms are at [LICENSES/LicenseRef-EeryFrank-Assets-Permission-Required.txt](LICENSES/LicenseRef-EeryFrank-Assets-Permission-Required.txt). It permits an unmodified asset to be used or distributed only as part of an unmodified official release package. Standalone extraction, reuse, modification, redistribution, commercial use, or branding use requires prior written permission from EeryFrank.

Configuration and program files do not become assets merely because they share a resource directory. Shader programs, `.mcmeta`, `sounds.json`, font-provider JSON, language JSON, loader metadata, and other functional data remain in the GPL scope unless a specific notice says otherwise. No current asset uses the new LicenseRef.

## Names, icons, and project identity

No code or asset license grants trademark rights in the project name, official icon, or other source identifiers, and no use may imply unauthorized endorsement. Accurate attribution, compatibility statements, and factual references remain allowed.

## Third-party and generated material

Third-party code, generated third-party files, tools, dependencies, mappings, assets, metadata, and quoted material retain their original terms. They are not relicensed by being referenced or stored here. The Gradle Wrapper remains Apache-2.0; Minecraft and official mappings remain subject to Mojang/Microsoft terms. Repository-held third-party material and the historical texture correction are described in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Embedded C2PA/JUMBF data is provenance evidence, not a complete ownership or licensing determination. Standard license-text copies provide their respective legal terms and are not themselves reassigned by their repository path.
