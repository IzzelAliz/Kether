package io.izzel.kether.bukkit.actions;

import io.izzel.kether.bukkit.BukkitQuestContext;
import io.izzel.kether.bukkit.KetherPlugin;
import io.izzel.kether.bukkit.util.Closables;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

final class TimeoutAction implements QuestAction<Void, BukkitQuestContext> {

    private final long ticks;

    public TimeoutAction(long ticks) {
        this.ticks = ticks;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public CompletableFuture<Void> process(BukkitQuestContext context) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        context.addClosable(Closables.bukkitTask(
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                KetherPlugin.instance(),
                () -> future.complete(null),
                ticks
            )
        ));
        return future;
    }

    @Override
    public String getDataPrefix() {
        return "timeout";
    }

    @Override
    public String toString() {
        return "TimeoutAction{" +
            "ticks=" + ticks +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.<Void, BukkitQuestContext>of(
            resolver -> new TimeoutAction(resolver.nextDuration() / 50L),
            KetherCompleters.consume()
        );
    }
}
