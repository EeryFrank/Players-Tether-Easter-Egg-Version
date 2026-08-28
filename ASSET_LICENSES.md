<!-- SPDX-License-Identifier: LGPL-3.0-or-later -->

# Asset license inventory

This inventory is authoritative for non-code files that need a license different from the default LGPL project scope.

| File | SHA-256 | License | Record |
| --- | --- | --- | --- |
| `art/players-tether-icon-source.png` | `60F365BF8A51C0E8EC37014A5C32376DCA79DF21A1F21EC2E286898E2C803F1E` | `MIT` | Source icon present in the original MIT project; the `1.0.0` changelog describes it as original voxel art. Embedded C2PA/JUMBF strings identify `gpt-image`, `trainedAlgorithmicMedia`, and `OpenAI Media Service API`; the original account, inputs, and prompt were not recorded. |
| `common/src/main/resources/players_tether_icon.png` | `DE025391AEFDEE814DE509B41393F7CD21583DFC3FB9243B3A4D70AE78E51C29` | `MIT` | Packaged icon derived for the original MIT release. |
| `docs/assets/players_tether_icon.png` | `DE025391AEFDEE814DE509B41393F7CD21583DFC3FB9243B3A4D70AE78E51C29` | `MIT` | Documentation copy of the packaged icon. |
| `art/generated/tamed-effect-source.png` | `590300FF87BA43D8E8D5F1A23875C7A5709A60D28DCFB2D732464645B0AB7AF6` | `CC-BY-SA-4.0` | Original chain-link effect art generated for this project with OpenAI's built-in image generation tool on 2026-08-28, without reference images. Prompt and processing record: `art/generated/tamed-effect-prompt.md`. |
| `common/src/main/resources/assets/qizhang_player_leash/textures/mob_effect/tamed.png` | `7F6FBB7CFA2F6A277EE7F1E39A4CDAD701CC2461936CD66986FF15BA69A00B46` | `CC-BY-SA-4.0` | 16x16 derivative of the recorded source, cropped and downscaled with nearest-neighbour sampling; replaces a historical file that was identical to Mojang's bone texture. |

The historical MIT grant for the three icon files above remains available at [LICENSES/MIT.txt](LICENSES/MIT.txt). They are intentionally not moved to `CC-BY-SA-4.0` in this change. The two new effect-art files are licensed under [CC-BY-SA-4.0](LICENSES/CC-BY-SA-4.0.txt). Embedded C2PA/JUMBF signature and issuer metadata is retained as provenance evidence and is not claimed as project-original or independently relicensed.

`NEEDS_MANUAL_VALIDATION`: embedded provenance strings are not a signature or rights verification. Before replacing or newly relicensing an image, confirm its creator or generating account, source inputs, any generative-tool terms, Minecraft-derived elements, and authority to grant the new license. Do not describe either source image as exclusively human-made. New CC assets must be added here with creator, source, license, modification notes, and a fixed hash.
