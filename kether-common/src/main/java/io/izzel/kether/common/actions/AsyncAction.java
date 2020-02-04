package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.concurrent.CompletableFuture;

final class AsyncAction<T, CTX extends QuestContext> implements QuestAction<T, CTX> {

    private final QuestAction<T, CTX> action;

    public AsyncAction(QuestAction<T, CTX> action) {
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public CompletableFuture<T> process(CTX context) {
        return context.runAction("async", action);
    }

    @Override
    public String toString() {
        return "AsyncAction{" +
            "action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AsyncAction<>(resolver.nextAction()),
            KetherTypes.completeAction(service)
        );
    }
}
