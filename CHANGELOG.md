# Changelog

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
