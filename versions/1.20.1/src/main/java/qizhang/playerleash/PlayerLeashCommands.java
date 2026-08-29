// SPDX-License-Identifier: GPL-3.0-only

package qizhang.playerleash;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

final class PlayerLeashCommands {
    private static final Field COMMAND_ORIGIN_FIELD = findCommandOriginField();

    private PlayerLeashCommands() {
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(createRoot("qzleash"));
        dispatcher.register(createRoot("玩家拴绳"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRoot(String name) {
        return literal(name)
                .requires(PlayerLeashCommands::canAdmin)
                .executes(context -> status(context.getSource()))
                .then(literal("status").executes(context -> status(context.getSource())))
                .then(literal("default")
                        .then(literal("allow").executes(context -> setDefault(context.getSource(), true)))
                        .then(literal("deny").executes(context -> setDefault(context.getSource(), false))))
                .then(literal("allow")
                        .then(playerPairArguments(true)))
                .then(literal("deny")
                        .then(playerPairArguments(false)))
                .then(literal("clear")
                        .then(holderArgument()
                                .then(targetArgument()
                                        .executes(context -> clearRule(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "holder"),
                                                StringArgumentType.getString(context, "target"))))))
                .then(literal("check")
                        .then(holderArgument()
                                .then(targetArgument()
                                        .executes(context -> checkRule(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "holder"),
                                                StringArgumentType.getString(context, "target"))))))
                .then(literal("list").executes(context -> listRules(context.getSource())))
                .then(literal("reload").executes(context -> reload(context.getSource())))
                .then(literal("release")
                        .then(literal("all").executes(context -> releaseAll(context.getSource())))
                        .then(targetArgument().executes(context -> releaseNamed(
                                context.getSource(),
                                StringArgumentType.getString(context, "target")))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
            playerPairArguments(boolean allowed) {
        return holderArgument().then(targetArgument().executes(context -> setRule(
                context.getSource(),
                StringArgumentType.getString(context, "holder"),
                StringArgumentType.getString(context, "target"),
                allowed)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
            holderArgument() {
        return argument("holder", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getOnlinePlayerNames(), builder));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String>
            targetArgument() {
        return argument("target", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        context.getSource().getOnlinePlayerNames(), builder));
    }

    private static int status(CommandSourceStack source) {
        LeashRuleStore store = store(source);
        source.sendSuccess(() -> Component.literal(
                "[玩家拴绳] 版本=" + QizhangPlayerLeash.VERSION
                        + "，默认=" + (store.defaultAllowed() ? "允许" : "禁止")
                        + "，定向规则=" + store.ruleCount() + " 条")
                .withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal(
                "用法：/qzleash allow|deny|clear <拴人者> <被拴者>；/qzleash default allow|deny")
                .withStyle(ChatFormatting.GRAY), false);
        return 1;
    }

    private static int setDefault(CommandSourceStack source, boolean allowed) {
        try {
            store(source).setDefaultAllowed(allowed);
            int released = QizhangPlayerLeash.manager().releaseDisallowed(source.getServer());
            source.sendSuccess(() -> Component.literal(
                    "[玩家拴绳] 默认规则已改为" + (allowed ? "允许" : "禁止")
                            + "，自动解除 " + released + " 条不再允许的拴绳。")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (IOException exception) {
            return fail(source, "保存默认规则失败：" + exception.getMessage());
        }
    }

    private static int setRule(CommandSourceStack source, String holder, String target, boolean allowed) {
        try {
            store(source).setRule(holder, target, allowed);
            int released = QizhangPlayerLeash.manager().releaseDisallowed(source.getServer());
            source.sendSuccess(() -> Component.literal(
                    "[玩家拴绳] " + holder + " -> " + target + " = "
                            + (allowed ? "允许" : "禁止")
                            + "；解除 " + released + " 条不再允许的拴绳。")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (IOException | IllegalArgumentException exception) {
            return fail(source, "保存定向规则失败：" + exception.getMessage());
        }
    }

    private static int clearRule(CommandSourceStack source, String holder, String target) {
        try {
            boolean removed = store(source).clearRule(holder, target);
            int released = QizhangPlayerLeash.manager().releaseDisallowed(source.getServer());
            source.sendSuccess(() -> Component.literal(
                    "[玩家拴绳] " + holder + " -> " + target
                            + (removed ? " 的定向规则已删除" : " 没有定向规则")
                            + "；当前按默认规则处理，解除 " + released + " 条。")
                    .withStyle(removed ? ChatFormatting.GREEN : ChatFormatting.YELLOW), true);
            return removed ? 1 : 0;
        } catch (IOException | IllegalArgumentException exception) {
            return fail(source, "删除定向规则失败：" + exception.getMessage());
        }
    }

    private static int checkRule(CommandSourceStack source, String holder, String target) {
        if (!LeashRuleStore.validName(holder) || !LeashRuleStore.validName(target)) {
            return fail(source, "玩家名必须为 1-16 位英文字母、数字或下划线。");
        }
        boolean allowed = store(source).isAllowed(holder, target);
        source.sendSuccess(() -> Component.literal(
                "[玩家拴绳] " + holder + " -> " + target + "：" + (allowed ? "允许" : "禁止"))
                .withStyle(allowed ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        return allowed ? 1 : 0;
    }

    private static int listRules(CommandSourceStack source) {
        LeashRuleStore store = store(source);
        List<LeashRuleStore.RuleEntry> entries = store.entries();
        status(source);
        if (entries.isEmpty()) {
            source.sendSuccess(() -> Component.literal("[玩家拴绳] 当前没有定向规则。")
                    .withStyle(ChatFormatting.GRAY), false);
            return 1;
        }
        int shown = Math.min(entries.size(), 50);
        for (int index = 0; index < shown; index++) {
            LeashRuleStore.RuleEntry entry = entries.get(index);
            source.sendSuccess(() -> Component.literal(
                    " - " + entry.holder() + " -> " + entry.target() + " = "
                            + (entry.allowed() ? "允许" : "禁止"))
                    .withStyle(entry.allowed() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
        }
        if (entries.size() > shown) {
            source.sendSuccess(() -> Component.literal(
                    "[玩家拴绳] 仅显示前 " + shown + " 条，共 " + entries.size() + " 条。")
                    .withStyle(ChatFormatting.YELLOW), false);
        }
        return entries.size();
    }

    private static int reload(CommandSourceStack source) {
        try {
            store(source).reload();
            int released = QizhangPlayerLeash.manager().releaseDisallowed(source.getServer());
            source.sendSuccess(() -> Component.literal(
                    "[玩家拴绳] 规则文件已重新加载，解除 " + released + " 条不再允许的拴绳。")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (IOException exception) {
            return fail(source, "重新加载失败：" + exception.getMessage());
        }
    }

    private static int releaseAll(CommandSourceStack source) {
        int released = QizhangPlayerLeash.manager().releaseAll(
                source.getServer(), true, "管理员解除了拴绳。");
        source.sendSuccess(() -> Component.literal(
                "[玩家拴绳] 已解除全部 " + released + " 条玩家拴绳。")
                .withStyle(ChatFormatting.GREEN), true);
        return released;
    }

    private static int releaseNamed(CommandSourceStack source, String target) {
        int released = QizhangPlayerLeash.manager().releaseNamed(
                source.getServer(), target, "管理员解除了拴绳。");
        if (released == 0) {
            return fail(source, "没有找到在线且正被拴住的玩家：" + target);
        }
        source.sendSuccess(() -> Component.literal(
                "[玩家拴绳] 已解除 " + target + " 的拴绳。")
                .withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static LeashRuleStore store(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        return QizhangPlayerLeash.manager().rules(server);
    }

    private static int fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal("[玩家拴绳] " + message).withStyle(ChatFormatting.RED));
        return 0;
    }

    static boolean canAdmin(CommandSourceStack source) {
        CommandSource origin = commandOrigin(source);
        if (origin instanceof ServerPlayer player) {
            return player.getClass() == ServerPlayer.class
                    && player.connection != null
                    && player.connection.isAcceptingMessages()
                    && source.getEntity() == player
                    && source.hasPermission(4)
                    && player.createCommandSourceStack().hasPermission(4);
        }
        return origin == source.getServer()
                && source.getEntity() == null
                && source.hasPermission(4)
                && "server".equals(source.getTextName().toLowerCase(Locale.ROOT));
    }

    private static Field findCommandOriginField() {
        Field match = null;
        for (Field field : CommandSourceStack.class.getDeclaredFields()) {
            if (field.getType() != CommandSource.class) {
                continue;
            }
            if (match != null) {
                return null;
            }
            match = field;
        }
        return match != null && match.trySetAccessible() ? match : null;
    }

    private static CommandSource commandOrigin(CommandSourceStack source) {
        if (COMMAND_ORIGIN_FIELD == null) {
            return null;
        }
        try {
            return (CommandSource) COMMAND_ORIGIN_FIELD.get(source);
        } catch (IllegalAccessException | RuntimeException ignored) {
            return null;
        }
    }
}
