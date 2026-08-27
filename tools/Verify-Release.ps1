[CmdletBinding()]
param(
    [string]$ReleaseDirectory
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
if ([string]::IsNullOrWhiteSpace($ReleaseDirectory)) {
    $ReleaseDirectory = Join-Path $repositoryRoot 'build/release'
}
$ReleaseDirectory = [IO.Path]::GetFullPath($ReleaseDirectory)

function Assert-Release {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw "Release verification failed: $Message"
    }
}

function Read-ZipText {
    param(
        [IO.Compression.ZipArchive]$Archive,
        [string]$EntryName
    )

    $entry = $Archive.GetEntry($EntryName)
    Assert-Release ($null -ne $entry) "missing JAR entry '$EntryName'"
    $reader = [IO.StreamReader]::new($entry.Open(), [Text.Encoding]::UTF8, $true)
    try {
        return $reader.ReadToEnd()
    }
    finally {
        $reader.Dispose()
    }
}

function Read-ClassMajorVersion {
    param(
        [IO.Compression.ZipArchive]$Archive,
        [string]$EntryName
    )

    $entry = $Archive.GetEntry($EntryName)
    Assert-Release ($null -ne $entry) "missing class '$EntryName'"
    $stream = $entry.Open()
    try {
        $header = [byte[]]::new(8)
        $offset = 0
        while ($offset -lt $header.Length) {
            $read = $stream.Read($header, $offset, $header.Length - $offset)
            if ($read -eq 0) {
                break
            }
            $offset += $read
        }
        Assert-Release ($offset -eq 8) "truncated class '$EntryName'"
        Assert-Release (
            $header[0] -eq 0xCA -and $header[1] -eq 0xFE -and
            $header[2] -eq 0xBA -and $header[3] -eq 0xBE
        ) "invalid class magic for '$EntryName'"
        return ([int]$header[6] * 256) + [int]$header[7]
    }
    finally {
        $stream.Dispose()
    }
}

function Read-ZipBytes {
    param(
        [IO.Compression.ZipArchive]$Archive,
        [string]$EntryName
    )

    $entry = $Archive.GetEntry($EntryName)
    Assert-Release ($null -ne $entry) "missing JAR entry '$EntryName'"
    $stream = $entry.Open()
    $buffer = [IO.MemoryStream]::new()
    try {
        $stream.CopyTo($buffer)
        return $buffer.ToArray()
    }
    finally {
        $buffer.Dispose()
        $stream.Dispose()
    }
}

function Test-ClassText {
    param(
        [IO.Compression.ZipArchive]$Archive,
        [string]$EntryName,
        [string]$Text
    )

    $bytes = Read-ZipBytes $Archive $EntryName
    return [Text.Encoding]::ASCII.GetString($bytes).Contains($Text)
}

function Test-ClassUtf8Text {
    param(
        [IO.Compression.ZipArchive]$Archive,
        [string]$EntryName,
        [string]$Text
    )

    $bytes = Read-ZipBytes $Archive $EntryName
    return [Text.Encoding]::UTF8.GetString($bytes).Contains($Text)
}

Assert-Release (Test-Path -LiteralPath $ReleaseDirectory -PathType Container) "release directory not found: $ReleaseDirectory"

$properties = @{}
foreach ($line in Get-Content -LiteralPath (Join-Path $repositoryRoot 'gradle.properties')) {
    $trimmed = $line.Trim()
    if ($trimmed.Length -eq 0 -or $trimmed.StartsWith('#')) {
        continue
    }
    $pair = $trimmed.Split([char[]]'=', 2)
    if ($pair.Count -eq 2) {
        $properties[$pair[0].Trim()] = $pair[1].Trim()
    }
}

$modVersion = $properties['mod_version']
$archiveName = $properties['archives_name']
Assert-Release (-not [string]::IsNullOrWhiteSpace($modVersion)) 'mod_version is missing from gradle.properties'
Assert-Release (-not [string]::IsNullOrWhiteSpace($archiveName)) 'archives_name is missing from gradle.properties'

$targets = @(
    [pscustomobject]@{ Name = 'fabric-1.20.1';   Minecraft = '1.20.1'; Java = 17; Major = 61; Pack = 15; Metadata = 'fabric.mod.json';             InteractionGuard = $false; CompanionOrdering = $false; VanillaLeashInvoker = $false; ClientEvent = 'qizhang/playerleash/QizhangPlayerLeashClient.class';                 ClientCallback = 'onInitializeClient'; ClientRegistration = 'net/fabricmc/api/ClientModInitializer' },
    [pscustomobject]@{ Name = 'fabric-1.21.1';   Minecraft = '1.21.1'; Java = 21; Major = 65; Pack = 34; Metadata = 'fabric.mod.json';             InteractionGuard = $true;  CompanionOrdering = $false; VanillaLeashInvoker = $true;  ClientEvent = 'qizhang/playerleash/QizhangPlayerLeashClient.class';                 ClientCallback = 'onInitializeClient'; ClientRegistration = 'net/fabricmc/api/ClientModInitializer' },
    [pscustomobject]@{ Name = 'forge-1.20.1';    Minecraft = '1.20.1'; Java = 17; Major = 61; Pack = 15; Metadata = 'META-INF/mods.toml';          InteractionGuard = $false; CompanionOrdering = $false; VanillaLeashInvoker = $false; ClientEvent = 'qizhang/playerleash/client/ForgeLikeClientRenderEvents.class';         ClientCallback = 'onRenderLevel';       ClientRegistration = 'net/minecraftforge/fml/common/Mod$EventBusSubscriber' },
    [pscustomobject]@{ Name = 'forge-1.21.1';    Minecraft = '1.21.1'; Java = 21; Major = 65; Pack = 34; Metadata = 'META-INF/mods.toml';          InteractionGuard = $true;  CompanionOrdering = $false; VanillaLeashInvoker = $true;  ClientEvent = 'qizhang/playerleash/client/ForgeClientRenderEvents.class';             ClientCallback = 'onRenderLevel';       ClientRegistration = 'net/minecraftforge/fml/common/Mod$EventBusSubscriber' },
    [pscustomobject]@{ Name = 'neoforge-1.20.1'; Minecraft = '1.20.1'; Java = 17; Major = 61; Pack = 15; Metadata = 'META-INF/mods.toml';          InteractionGuard = $false; CompanionOrdering = $false; VanillaLeashInvoker = $false; ClientEvent = 'qizhang/playerleash/client/ForgeLikeClientRenderEvents.class';         ClientCallback = 'onRenderLevel';       ClientRegistration = 'net/minecraftforge/fml/common/Mod$EventBusSubscriber' },
    [pscustomobject]@{ Name = 'neoforge-1.21.1'; Minecraft = '1.21.1'; Java = 21; Major = 65; Pack = 34; Metadata = 'META-INF/neoforge.mods.toml'; InteractionGuard = $true;  CompanionOrdering = $true;  VanillaLeashInvoker = $true;  ClientEvent = 'qizhang/playerleash/client/NeoForgeClientRenderEvents.class';          ClientCallback = 'onRenderLevel';       ClientRegistration = 'net/neoforged/fml/common/EventBusSubscriber' }
)

$jarFiles = @(Get-ChildItem -LiteralPath $ReleaseDirectory -Filter '*.jar' -File | Sort-Object Name)
Assert-Release ($jarFiles.Count -eq $targets.Count) "expected 6 JARs, found $($jarFiles.Count)"

Add-Type -AssemblyName System.IO.Compression.FileSystem
$metadataPaths = @('fabric.mod.json', 'META-INF/mods.toml', 'META-INF/neoforge.mods.toml')
$requiredEntries = @(
    'pack.mcmeta',
    'qizhang-player-leash.mixins.json',
    'qizhang-player-leash.refmap.json',
    'qizhang/playerleash/QizhangPlayerLeash.class',
    'qizhang/playerleash/PlayerLeashManager.class',
    'qizhang/playerleash/TamedVisualRules.class',
    'qizhang/playerleash/client/FirstPersonLeashRenderer.class',
    'qizhang/playerleash/client/TamedPlayerRenderer.class',
    'qizhang/playerleash/mixin/PlayerLeashMixin.class',
    'qizhang/playerleash/mixin/client/PlayerRendererMixin.class',
    'players_tether_icon.png',
    'META-INF/LICENSE-qizhang_player_leash'
)
$checksumLines = [Collections.Generic.List[string]]::new()

foreach ($target in $targets) {
    $expectedName = "$archiveName-$($target.Name)-$modVersion.jar"
    $jar = $jarFiles | Where-Object Name -eq $expectedName
    Assert-Release ($null -ne $jar) "missing expected JAR '$expectedName'"

    $archive = [IO.Compression.ZipFile]::OpenRead($jar.FullName)
    try {
        $entryNames = @($archive.Entries | ForEach-Object FullName)
        foreach ($entryName in $requiredEntries) {
            Assert-Release ($entryNames -contains $entryName) "$expectedName is missing '$entryName'"
        }
        foreach ($metadataPath in $metadataPaths) {
            $shouldExist = $metadataPath -eq $target.Metadata
            Assert-Release (($entryNames -contains $metadataPath) -eq $shouldExist) "$expectedName has an invalid loader metadata set"
        }

        $interactionMixinClass = 'qizhang/playerleash/mixin/PlayerEntityInteractMixin.class'
        Assert-Release (
            (($entryNames -contains $interactionMixinClass) -eq $target.InteractionGuard)
        ) "$expectedName has the wrong version-specific interaction Mixin class set"

        $leashInvokerClass = 'qizhang/playerleash/mixin/client/EntityRendererInvoker.class'
        Assert-Release (
            (($entryNames -contains $leashInvokerClass) -eq $target.VanillaLeashInvoker)
        ) "$expectedName has the wrong version-specific leash invoker class set"
        Assert-Release (
            $entryNames -contains $target.ClientEvent
        ) "$expectedName is missing its loader-specific client render bridge"

        $metadataText = Read-ZipText $archive $target.Metadata
        if ($target.Metadata -eq 'fabric.mod.json') {
            $fabricMetadata = $metadataText | ConvertFrom-Json
            Assert-Release ($fabricMetadata.id -eq 'qizhang_player_leash') "$expectedName has the wrong Fabric mod id"
            Assert-Release ($fabricMetadata.version -eq $modVersion) "$expectedName has the wrong Fabric mod version"
            Assert-Release ($fabricMetadata.depends.minecraft -eq "=$($target.Minecraft)") "$expectedName has the wrong Minecraft dependency"
            Assert-Release ($fabricMetadata.depends.java -eq ">=$($target.Java)") "$expectedName has the wrong Java dependency"
            Assert-Release (
                @($fabricMetadata.entrypoints.client) -contains 'qizhang.playerleash.QizhangPlayerLeashClient'
            ) "$expectedName does not register its Fabric client entry point"
        }
        else {
            Assert-Release ([regex]::IsMatch($metadataText, '(?m)^\s*modId\s*=\s*"qizhang_player_leash"\s*$')) "$expectedName has the wrong Forge-family mod id"
            $versionPattern = '(?m)^\s*version\s*=\s*"' + [regex]::Escape($modVersion) + '"\s*$'
            Assert-Release ([regex]::IsMatch($metadataText, $versionPattern)) "$expectedName has the wrong Forge-family mod version"
            $minecraftPattern = '(?m)^\s*versionRange\s*=\s*"\[' + [regex]::Escape($target.Minecraft) + '\]"\s*$'
            Assert-Release ([regex]::IsMatch($metadataText, $minecraftPattern)) "$expectedName has the wrong Minecraft dependency"
        }

        $hasCompanionOrdering = [regex]::IsMatch(
            $metadataText,
            '(?m)^\s*modId\s*=\s*"qizhang_aquaculture_turtle_companion"\s*$'
        )
        Assert-Release (
            $hasCompanionOrdering -eq $target.CompanionOrdering
        ) "$expectedName has the wrong optional companion metadata scope"
        if ($target.CompanionOrdering) {
            $companionBlockPattern = '(?ms)' +
                    '\[\[dependencies\.qizhang_player_leash\]\]\s*' +
                    'modId\s*=\s*"qizhang_aquaculture_turtle_companion"\s*' +
                    'type\s*=\s*"optional"\s*' +
                    'versionRange\s*=\s*"\[1\.0\.0,\)"\s*' +
                    'ordering\s*=\s*"BEFORE"\s*' +
                    'side\s*=\s*"SERVER"\s*'
            Assert-Release ([regex]::IsMatch($metadataText, $companionBlockPattern)) "$expectedName has an invalid optional companion dependency block"
        }

        $packMetadata = (Read-ZipText $archive 'pack.mcmeta') | ConvertFrom-Json
        Assert-Release ([int]$packMetadata.pack.pack_format -eq $target.Pack) "$expectedName has the wrong resource-pack format"

        $mixinMetadata = (Read-ZipText $archive 'qizhang-player-leash.mixins.json') | ConvertFrom-Json
        Assert-Release ($mixinMetadata.required -eq $true) "$expectedName does not require its Mixin config"
        Assert-Release ($mixinMetadata.refmap -eq 'qizhang-player-leash.refmap.json') "$expectedName has the wrong refmap name"
        Assert-Release ($mixinMetadata.compatibilityLevel -eq "JAVA_$($target.Java)") "$expectedName has the wrong Mixin Java level"
        $mixinNames = @($mixinMetadata.mixins)
        $clientMixinNames = @($mixinMetadata.client)
        Assert-Release ($mixinNames -contains 'PlayerLeashMixin') "$expectedName does not register PlayerLeashMixin"
        Assert-Release ($clientMixinNames -contains 'client.PlayerRendererMixin') "$expectedName does not register PlayerRendererMixin"
        Assert-Release (
            (($mixinNames -contains 'PlayerEntityInteractMixin') -eq $target.InteractionGuard)
        ) "$expectedName has the wrong version-specific Mixin registration set"
        Assert-Release (
            (($clientMixinNames -contains 'client.EntityRendererInvoker') -eq $target.VanillaLeashInvoker)
        ) "$expectedName has the wrong version-specific client invoker registration set"

        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/QizhangPlayerLeash.class' $modVersion)
        ) "$expectedName does not expose runtime version $modVersion"
        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/PlayerLeashManager.class' 'broadcastTamingEffectToTrackingClients')
        ) "$expectedName does not broadcast the refreshed taming effect to tracking clients"
        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/PlayerLeashManager.class' 'showsHeartParticles')
        ) "$expectedName does not use the shared level-six heart rule"
        Assert-Release (
            -not (Test-ClassUtf8Text $archive 'qizhang/playerleash/PlayerLeashManager.class' '驯服效果提升到')
        ) "$expectedName still sends duplicate layer-progress chat to the tethered player"
        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/TamedVisualRules.class' 'usesWolfModel') -and
            (Test-ClassText $archive 'qizhang/playerleash/TamedVisualRules.class' 'showsHeartParticles')
        ) "$expectedName is missing the shared wolf/heart visual rules"
        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/client/TamedPlayerRenderer.class' 'usesWolfModel') -and
            (Test-ClassText $archive 'qizhang/playerleash/client/TamedPlayerRenderer.class' 'syncWolfLeashHolder')
        ) "$expectedName does not keep the wolf proxy's tether lifecycle synchronized"
        Assert-Release (
            (Test-ClassText $archive 'qizhang/playerleash/client/FirstPersonLeashRenderer.class' 'shouldRenderLocalFirstPersonLeash') -and
            (Test-ClassText $archive $target.ClientEvent 'AFTER_ENTITIES') -and
            (Test-ClassText $archive $target.ClientEvent $target.ClientCallback) -and
            (Test-ClassText $archive $target.ClientEvent $target.ClientRegistration)
        ) "$expectedName is missing the first-person AFTER_ENTITIES rope path"
        if ($target.VanillaLeashInvoker) {
            Assert-Release (
                (Test-ClassText $archive 'qizhang/playerleash/client/FirstPersonLeashRenderer.class' 'qizhang$renderLeash')
            ) "$expectedName does not invoke the Minecraft 1.21.1 vanilla leash renderer"
        }
        else {
            Assert-Release (
                (Test-ClassText $archive 'qizhang/playerleash/client/FirstPersonLeashRenderer.class' 'PlayerLeashRenderer')
            ) "$expectedName does not reuse the Minecraft 1.20.1 custom leash renderer"
        }
        if ($target.InteractionGuard) {
            Assert-Release (
                (Test-ClassText $archive 'qizhang/playerleash/mixin/PlayerLeashMixin.class' 'qizhang$keepElasticBeyondVanillaRange')
            ) "$expectedName is missing the elastic too-far override"
            Assert-Release (
                -not (Test-ClassText $archive 'qizhang/playerleash/mixin/PlayerLeashMixin.class' 'qizhang$tickPlayerLeash')
            ) "$expectedName still contains the duplicate player leash tick"
            Assert-Release (
                (Test-ClassText $archive $interactionMixinClass 'qizhang$skipVanillaLeashToggleForPlayerTarget')
            ) "$expectedName is missing the player interaction guard"
        }
        else {
            Assert-Release (
                -not (Test-ClassText $archive 'qizhang/playerleash/PlayerLeashManager.class' 'BREAK_RANGE')
            ) "$expectedName still contains the removed hard break range"
        }

        $refmapEntry = $archive.GetEntry('qizhang-player-leash.refmap.json')
        Assert-Release ($refmapEntry.Length -gt 2) "$expectedName has an empty refmap"
        $null = (Read-ZipText $archive 'qizhang-player-leash.refmap.json') | ConvertFrom-Json

        $major = Read-ClassMajorVersion $archive 'qizhang/playerleash/QizhangPlayerLeash.class'
        Assert-Release ($major -eq $target.Major) "$expectedName has class major $major, expected $($target.Major)"
    }
    finally {
        $archive.Dispose()
    }

    $hash = (Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256).Hash.ToUpperInvariant()
    $checksumLines.Add("$hash  $expectedName")
    Write-Host "RELEASE_JAR=PASS target=$($target.Name) java=$($target.Java) sha256=$hash"
}

$checksumPath = Join-Path $ReleaseDirectory 'SHA256SUMS.txt'
$utf8NoBom = [Text.UTF8Encoding]::new($false)
[IO.File]::WriteAllLines($checksumPath, [string[]]$checksumLines, $utf8NoBom)

Write-Host "RELEASE_VERIFY=PASS jars=$($targets.Count) checksum=$checksumPath"
