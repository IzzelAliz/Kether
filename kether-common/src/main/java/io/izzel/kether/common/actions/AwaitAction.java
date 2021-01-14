package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.data.QuestFuture;
import io.izzel.kether.common.api.QuestService;

import java.util.concurrent.CompletableFuture;

final class AwaitAction<T> extends QuestAction<T> {

    private final QuestAction<T> action;

    public AwaitAction(QuestAction<T> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<T> process(QuestContext.Frame frame) {
        CompletableFuture<T> future = new CompletableFuture<>();
        frame.newFrame(action).<T>run().thenAccept(QuestFuture.complete(future));
        return future;
    }

    @Override
    public String toString() {
        return "AwaitAction{" +
            "action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AwaitAction<>(resolver.nextAction()),
            KetherCompleters.action(service)
        );
    }
}
