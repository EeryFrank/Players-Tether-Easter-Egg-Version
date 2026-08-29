<!-- SPDX-License-Identifier: GPL-3.0-only -->

# Player's Tether_Easter Egg Version 中文说明

作者：**QiZhang**

许可证：**GPL-3.0-only（当前代码与功能内容）+ MIT（3 张历史项目图标）+ CC-BY-SA-4.0（2 张既有效果图）；未来新增的自有美术、音频和品牌资产默认须事先授权。**

## 环境与安装

| Minecraft | 加载器构建目标 | Java | 发布状态 |
| --- | --- | ---: | --- |
| 1.21.1 | Fabric Loader 0.19.3 + Fabric API 0.116.15 | 21 | 支持 |
| 1.21.1 | NeoForge 21.1.244 | 21 | 支持 |
| 1.21.1 | Forge 52.1.0 | 21 | 支持 |
| 1.20.1 | Fabric Loader 0.19.3 + Fabric API 0.92.11 | 17 | 支持 |
| 1.20.1 | NeoForge 47.1.106 历史兼容线 | 17 | 实验性 |
| 1.20.1 | Forge 47.4.10 | 17 | 支持 |

服务端和所有客户端必须安装文件名中 Minecraft 版本、加载器都完全匹配的 JAR，并完整重启；六个 JAR 不能混用。

NeoForge 1.20.1 是已停止维护的短期 Forge 兼容分支。该目标仅为旧环境保留，不代表对现代 NeoForge 的支持，发布时按实验性版本处理。

在 NeoForge 1.21.1 服务端上，可选安装 QiZhang Aquaculture Turtle Companion
1.0.0+。玩家拴绳仅处理玩家目标，水产龟伙伴仅处理其拥有的非玩家生物；元数据只用于
确定加载顺序，不会让任何一方成为必装依赖。

## 玩家操作

1. 手持原版拴绳右键另一名玩家即可建立拴绳。
2. 拴人者潜行并右键自己拴住的玩家即可解除。
3. 生存玩家会消耗一根拴绳，解除时按原版逻辑掉落；创造玩家不会消耗，也不会额外生成拴绳。
4. 玩家离线、死亡、切换维度、持有者无效或后台规则变为禁止时会自动解除。
5. 有效的玩家拴绳超过原版十格限制后仍保持弹性拉回，不会仅因距离自动断开。
6. 被拴玩家自己的第一人称会显示绳线；第二、第三人称会让绳线继续连接玩家或第 3 层狼模型代理。
7. 模组会阻止循环拴绳关系。

## 驯服彩蛋

- 第 1～6 层依次需要连续 30、25、20、15、10、5 秒，总计 105 秒。
- 拴住期间效果持续刷新；解除后各层分别保留约 10、20、30、40、50、60 秒。
- 驯服效果会同步给被拴玩家及所有正在追踪该玩家的客户端，第三方观察者也能取得相同的模型状态。
- 被拴玩家从效果 HUD 查看当前层数，不再收到重复的升级聊天；拴人者仍会收到进度通知。
- 第 3 层起，客户端把玩家显示模型替换成原版驯服狼，仅改变画面，不改变玩家数据、背包、权限或碰撞箱。
- 第 6 层在玩家仍被拴住时会持续冒爱心；解除后即使效果仍保留约 60 秒，爱心也会停止。

## 后台规则

默认所有玩家都能拴其他玩家。规则按“拴人者 -> 被拴者”定向生效，不会自动反向。

只有权限 4 的真人管理员或服务器本地控制台能修改。RCON、命令方块、函数、`/execute` 包装来源和权限 0～3 均被拒绝。

```text
/qzleash status
/qzleash default allow|deny
/qzleash allow|deny|clear|check <拴人者> <被拴者>
/qzleash list
/qzleash reload
/qzleash release <被拴者>|all
```

中文根指令 `/玩家拴绳` 与 `/qzleash` 等价。

规则文件：`<世界>/serverconfig/qizhang-player-leash.properties`

## 构建

使用 Java 21 启动 Gradle；构建脚本会自动为 1.20.1 的编译和运行任务选择 Java 17：

```powershell
.\gradlew.bat --no-daemon --console=plain clean buildAll collectReleaseJars
pwsh -NoProfile -File .\tools\Verify-Release.ps1
```

六个发布 JAR 与 `SHA256SUMS.txt` 位于 `build/release/`。单独构建某个目标可使用例如 `.\gradlew.bat :fabric-1.20.1:build`。

## 开源许可

- 当前及后续的项目代码、测试、构建工具、功能数据、翻译和文档采用 `GPL-3.0-only`。
- 现有 3 张项目图标 PNG 继续适用历史 MIT 授权，精确路径和哈希见 [资产许可清单](../ASSET_LICENSES.md)。
- 现有牵绊效果源图与 16x16 纹理继续保留 `CC-BY-SA-4.0` 授权，不因本次政策变更而撤回。
- 未来新增的项目自有视觉、音频或品牌资产不会自动获得开放内容许可。完成来源与权利核验并在清单登记后，若无其他明确授权，则使用 `LicenseRef-EeryFrank-Assets-Permission-Required`：未修改资产只能随未经修改的官方完整包分发，单独提取、复用、修改、再分发、商业或品牌使用须事先取得书面授权。
- Gradle Wrapper 保留 Apache-2.0，其他第三方内容保留各自许可证；名称和官方图标不随代码许可证授予商标权。

`v1.0.0` 至 `v1.1.5` 已按 MIT 发布；其后的公开基线 `1d11c9bee63f17f51b554b4413c5e82e559f7ffa` 曾将项目原创源码与功能内容按 LGPL-3.0-or-later 提供。两类既有授权都不会被本次 GPL 变更撤回。完整边界见 [许可证政策](../LICENSE_POLICY.md)、[资产许可清单](../ASSET_LICENSES.md)和[第三方声明](../THIRD_PARTY_NOTICES.md)。

这些历史版本中的旧 `tamed.png` 与 Minecraft 原版骨头纹理字节相同，并不属于项目能够按 MIT 授权的内容。1.1.6 已用独立生成的 CC BY-SA 原创效果图替换。

## 验证边界

已覆盖六目标编译、自测、加载器元数据、按版本区分的 Mixin/refmap、资源包格式、Java 字节码版本和发布校验和；六个专用服务端目标均已启动到 `Done`、报告版本 1.1.5 并安全关闭。三个 1.20.1 客户端以及 Fabric、NeoForge 1.21.1 客户端均完成 LWJGL、声音与纹理渲染初始化。Forge 1.21.1 的 Loom userdev 客户端在模组加载前停于 Forge early-display 模块层初始化；其生产 JAR 仍已通过编译、发布校验及官方 Forge 52.1.0 专用服务端冒烟测试。

`NEEDS_MANUAL_VALIDATION`：仍需三名真人客户端（拴人者、被拴者、第三方观察者）验收玩家间状态同步、拉力与远距离行为、被拴者第一/第三人称绳线、第三方看到的第 3 层狼模型及第 6 层爱心；1.20.1 使用独立的同步与渲染实现，必须重点实测。
