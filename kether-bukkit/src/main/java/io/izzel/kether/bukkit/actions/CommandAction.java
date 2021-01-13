package io.izzel.kether.bukkit.actions;

import io.izzel.kether.bukkit.BukkitQuestContext;
import io.izzel.kether.common.api.data.ContextString;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.util.LocalizedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

final class CommandAction extends QuestAction<Boolean> {

    private final ContextString command;
    private final boolean console;
    private final boolean op;

    public CommandAction(ContextString command, boolean console, boolean op) {
        this.command = command;
        this.console = console;
        this.op = op;
    }

    @Override
    public CompletableFuture<Boolean> process(BukkitQuestContext context) {
        Player player = context.getPlayer();
        String s = command.get(context);
        if (console) {
            return CompletableFuture.completedFuture(
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s)
            );
        } else {
            boolean prev = player.isOp();
            if (op && !prev) {
                player.setOp(true);
            }
            CompletableFuture<Boolean> future = CompletableFuture.completedFuture(
                Bukkit.dispatchCommand(player, s)
            );
            if (op && !prev) {
                player.setOp(false);
            }
            return future;
        }
    }

    @Override
    public String toString() {
        return "CommandAction{" +
            "command='" + command + '\'' +
            ", console=" + console +
            ", op=" + op +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.<Boolean, BukkitQuestContext>of(
            resolver -> {
                boolean op = false, console = false;
                while (resolver.hasNext()) {
                    String s = resolver.nextElement();
                    if (s.equals("-o")) op = true;
                    else if (s.equals("-c")) console = true;
                    else {
                        return new CommandAction(resolver.contexted(s), console, op);
                    }
                }
                throw LocalizedException.of("not-match", "-c -o", "");
            },
            KetherCompleters.seq(
                KetherCompleters.optional(
                    KetherCompleters.constant("-c")
                ),
                KetherCompleters.optional(
                    KetherCompleters.constant("-o")
                ),
                KetherCompleters.consume()
            )
        );
    }
}
