package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.concurrent.CompletableFuture;

final class AwaitAction<T, CTX extends QuestContext> implements QuestAction<T, CTX> {

    private final QuestAction<T, CTX> action;

    public AwaitAction(QuestAction<T, CTX> action) {
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public CompletableFuture<T> process(CTX context) {
        CompletableFuture<T> future = new CompletableFuture<>();
        context.runAction("await", action).thenAccept(future::complete);
        return future;
    }

    @Override
    public String getDataPrefix() {
        return "await";
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
