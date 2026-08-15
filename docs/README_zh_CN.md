# Player's Tether_Easter Egg Version 中文说明

作者：**QiZhang**

许可证：**MIT**

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

## 玩家操作

1. 手持原版拴绳右键另一名玩家即可建立拴绳。
2. 拴人者潜行并右键自己拴住的玩家即可解除。
3. 生存玩家会消耗一根拴绳，解除时按原版逻辑掉落；创造玩家不会消耗，也不会额外生成拴绳。
4. 玩家离线、死亡、切换维度、持有者无效、距离过远或后台规则变为禁止时会自动解除。
5. 模组会阻止循环拴绳关系。

## 驯服彩蛋

- 第 1～6 层依次需要连续 30、25、20、15、10、5 秒，总计 105 秒。
- 拴住期间效果持续刷新；解除后各层分别保留约 10、20、30、40、50、60 秒。
- 第 3 层起，客户端把玩家显示模型替换成原版驯服狼，仅改变画面，不改变玩家数据、背包、权限或碰撞箱。
- 第 6 层会持续冒爱心，直到第 6 层效果结束。

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

## 验证边界

已覆盖六目标编译、自测、加载器元数据、Mixin/refmap、资源包格式、Java 字节码版本和发布校验和；六个专用服务端目标均已启动到 `Done` 并安全关闭，三个 1.20.1 目标确认使用 Java 17 且控制台状态命令正常。

`NEEDS_MANUAL_VALIDATION`：仍需两名真人客户端验收玩家间状态同步、拉力/断绳距离、绳线、狼模型和爱心视觉；1.20.1 使用独立的同步与渲染实现，必须重点实测。
