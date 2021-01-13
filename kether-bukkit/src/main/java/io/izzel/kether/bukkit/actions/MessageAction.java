package io.izzel.kether.bukkit.actions;

import io.izzel.kether.bukkit.BukkitQuestContext;
import io.izzel.kether.bukkit.KetherPlugin;
import io.izzel.kether.bukkit.util.Closables;
import io.izzel.kether.common.api.data.ContextString;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

final class MessageAction extends QuestAction<Void> {

    private final ContextString message;
    private final long timeoutTicks;

    public MessageAction(ContextString message, long timeoutTicks) {
        this.message = message;
        this.timeoutTicks = timeoutTicks;
    }

    private boolean isAsync() {
        return timeoutTicks != 0L;
    }

    @Override
    public CompletableFuture<Void> process(BukkitQuestContext context) {
        Player player = context.getPlayer();
        String s = message.get(context);
        if (isAsync()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            int task = Bukkit.getScheduler().scheduleSyncDelayedTask(
                KetherPlugin.instance(),
                () -> {
                    player.sendMessage(s);
                    future.complete(null);
                },
                timeoutTicks
            );
            context.addClosable(Closables.bukkitTask(task));
            return future;
        } else {
            player.sendMessage(s);
            return CompletableFuture.completedFuture(null);
        }
    }

    @Override
    public String toString() {
        return "MessageAction{" +
            "message='" + message + '\'' +
            ", timeoutTicks=" + timeoutTicks +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.<Void, BukkitQuestContext>of(
            resolver -> {
                String s = resolver.nextElement();
                if (s.equals("-t")) {
                    long timeout = resolver.nextLong();
                    return new MessageAction(resolver.nextContextString(), timeout);
                } else {
                    return new MessageAction(
                        resolver.contexted(s),
                        KetherPlugin.instance().getKetherConfig().getDefaultMessageTimeout()
                    );
                }
            },
            KetherCompleters.seq(
                KetherCompleters.optional(
                    KetherCompleters.seq(
                        KetherCompleters.constant("-t"),
                        KetherCompleters.consume()
                    )
                ),
                KetherCompleters.consume()
            )
        );
    }
}
