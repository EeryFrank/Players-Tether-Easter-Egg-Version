<!-- SPDX-License-Identifier: GPL-3.0-only -->
<!-- Asset-Default-License: LicenseRef-EeryFrank-Assets-Permission-Required -->

# Asset license inventory

This inventory is authoritative for non-code assets outside the repository's GPL source/functional scope. A path or file extension alone never assigns an asset license.

| File | SHA-256 | License | Record |
| --- | --- | --- | --- |
| `art/players-tether-icon-source.png` | `60F365BF8A51C0E8EC37014A5C32376DCA79DF21A1F21EC2E286898E2C803F1E` | `MIT` | Source icon present in the original MIT project; the `1.0.0` changelog describes it as original voxel art. Embedded C2PA/JUMBF strings identify `gpt-image`, `trainedAlgorithmicMedia`, and `OpenAI Media Service API`; the original account, inputs, and prompt were not recorded. |
| `common/src/main/resources/players_tether_icon.png` | `DE025391AEFDEE814DE509B41393F7CD21583DFC3FB9243B3A4D70AE78E51C29` | `MIT` | Packaged icon derived for the original MIT release. |
| `docs/assets/players_tether_icon.png` | `DE025391AEFDEE814DE509B41393F7CD21583DFC3FB9243B3A4D70AE78E51C29` | `MIT` | Documentation copy of the packaged icon. |
| `art/generated/tamed-effect-source.png` | `590300FF87BA43D8E8D5F1A23875C7A5709A60D28DCFB2D732464645B0AB7AF6` | `CC-BY-SA-4.0` | Original chain-link effect art generated for this project with OpenAI's built-in image generation tool on 2026-08-28, without reference images. Prompt and processing record: `art/generated/tamed-effect-prompt.md`. |
| `common/src/main/resources/assets/qizhang_player_leash/textures/mob_effect/tamed.png` | `7F6FBB7CFA2F6A277EE7F1E39A4CDAD701CC2461936CD66986FF15BA69A00B46` | `CC-BY-SA-4.0` | 16x16 derivative of the recorded source, cropped and downscaled with nearest-neighbour sampling; replaces a historical file that was identical to Mojang's bone texture. |

The three icon files retain their historical [MIT](LICENSES/MIT.txt) grants. The two effect-art files retain their existing [CC-BY-SA-4.0](LICENSES/CC-BY-SA-4.0.txt) grants. Those permissions are not withdrawn or replaced by this policy change. Embedded C2PA/JUMBF signature and issuer metadata remains provenance evidence and is not claimed as project-original or independently relicensed.

Future project-owned visual, audio, or branding assets require a rights review and an explicit row here. Unless that row records another expressly authorized license, the asset must use [LicenseRef-EeryFrank-Assets-Permission-Required](LICENSES/LicenseRef-EeryFrank-Assets-Permission-Required.txt). That LicenseRef allows an unmodified asset to travel only inside an unmodified official release package; standalone extraction, reuse, modification, redistribution, commercial use, or branding use requires prior written permission. No current asset uses that LicenseRef.

`NEEDS_MANUAL_VALIDATION`: embedded provenance strings are not a signature or rights verification. Before replacing, modifying, or newly licensing an image, confirm its creator or generating account, source inputs, applicable tool terms, Minecraft-derived elements, and authority. Do not describe either source image as exclusively human-made.
