<!-- SPDX-License-Identifier: LGPL-3.0-or-later -->

# Contributing

Issues and pull requests are welcome.

1. Use Java 21 to launch Gradle; the build selects Java 17 for Minecraft 1.20.1 targets.
2. Keep all six Minecraft/loader targets compatible unless a scoped change explicitly says otherwise.
3. Run `./gradlew --no-daemon --console=plain clean buildAll collectReleaseJars` and `tools/Verify-Release.ps1` before opening a pull request.
4. Do not weaken the direct-console/real-OP4 command-source checks without a security test.
5. Do not include server addresses, player data, world files, tokens, or private configuration.

## Contribution license

By submitting a contribution, you confirm that you have authority to provide it under the matching scope in [LICENSE_POLICY.md](LICENSE_POLICY.md):

- code, tests, build/CI files, functional data, localization, and documentation under `LGPL-3.0-or-later`;
- newly contributed original creative assets under `CC-BY-SA-4.0` only when `ASSET_LICENSES.md` records their creator, source, license, modifications, and hash;
- third-party content only under its original compatible license and with a complete notice.

Do not submit decompiled Minecraft code, vanilla assets, unclear-source media, or content whose terms prohibit redistribution or modification. State the provenance and applicable terms of commissioned, collaborative, or generative-tool output in the pull request.

Gameplay or rendering changes should include clear manual test steps for two real clients.
