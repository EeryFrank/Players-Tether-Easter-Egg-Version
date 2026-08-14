# Player's Tether_Easter Egg Version 中文说明

作者：**QiZhang**

许可证：**MIT**

## 环境与安装

- Minecraft 1.21.1
- NeoForge 21.1.244～21.1.x
- Java 21
- 服务端和所有客户端必须安装同一份 JAR，并完整重启

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

使用 Java 21：

```powershell
.\gradlew.bat clean build
```

产物位于 `build/libs/`。

## 验证边界

已覆盖编译、自测、JAR 结构、NeoForge 21.1.244 完整启动/安全关闭、控制台规则命令和 RCON 越权拒绝。两名真人客户端的拴绳手感、狼模型与爱心视觉仍属于发布前实机验收项。
