# Changelog

## 1.1.5 - 2026-08-27

- Ported the deployed single-loader 1.0.5 server/client fixes to Fabric,
  NeoForge, and Forge on both Minecraft 1.21.1 and 1.20.1.
- Rendered the tether rope in the tethered player's own first-person view while
  keeping the existing second/third-person player and wolf-proxy ropes.
- Broadcast taming-effect refreshes to the tethered player and every tracking
  client, so observers receive the state needed to render the level-3 wolf.
- Moved level-6 heart emission to the every-tick taming path, fixing a timing
  phase mismatch that could prevent hearts from appearing.
- Kept layer progress in the tethered player's effect HUD and removed their
  duplicate progress chat; the leash holder still receives each notification.
- Added shared deterministic rules for the level-3 wolf and level-6 hearts,
  plus release checks for the new synchronization and rendering paths.
- Preserved the 1.1.2 death, cross-dimension lead settlement, elastic-distance,
  and narrowly scoped player-interaction fixes during the port.

## 1.1.2 - 2026-08-16

- Ported the single-loader 1.0.2 server fixes to all three Minecraft 1.21.1
  loader builds.
- Prevented vanilla's second player interaction path from immediately removing
  a permission-checked tether established by the same right click.
- Removed the duplicate Minecraft 1.21.1 leash tick; the base entity tick
  already invokes `Leashable.tickLeash`.
- Kept valid player tethers elastic beyond vanilla's ten-block cutoff on both
  supported Minecraft versions instead of breaking solely because of distance.
- Limited the interaction bypass to a player target already tethered to the
  interacting player, preserving unrelated player and mob interactions.
- Routed player death through the mod's release manager before vanilla's static
  cleanup, preserving creative-mode no-drop and self-client detach semantics.
- Preserved the survival lead-consumption decision across dimension transfer so
  the after-change event can refund it after vanilla clears the holder link.
- Added attach-distance diagnostics and stricter release-JAR checks for the new
  1.21.1 interaction Mixin and removed duplicate-tick method.
- Added optional, server-side load ordering for QiZhang Aquaculture Turtle
  Companion on NeoForge 1.21.1 without introducing a runtime dependency.

## 1.1.0 - 2026-08-15

- Added separate Fabric, NeoForge, and Forge builds for Minecraft 1.21.1 and 1.20.1.
- Split loader entry points from shared rules, timing, and version-specific Minecraft logic.
- Added a Java 17-compatible 1.20.1 tether implementation with synchronized holder state, server-side pull/break physics, and client-side lead rendering.
- Preserved the vanilla 1.21.1 `Leashable` path and kept the lead visible when the level-3 wolf model is rendered.
- Added six-target CI, JAR structure verification, class-version checks, and SHA-256 release manifests.
- Marked NeoForge 1.20.1 as a legacy experimental target because its upstream 47.x line is no longer maintained.

## 1.0.0 - 2026-08-14

- Initial open-source release.
- Added player-to-player vanilla lead support.
- Added directional allow/deny rules with strict permission-level-4/local-console administration.
- Added six decreasing taming intervals: 30/25/20/15/10/5 seconds.
- Added the level-3 vanilla wolf render easter egg.
- Added persistent heart particles at level 6.
- Added English and Simplified Chinese localization and documentation.
- Added an original voxel-art mod icon showing a lead tethering a blank player mannequin.
