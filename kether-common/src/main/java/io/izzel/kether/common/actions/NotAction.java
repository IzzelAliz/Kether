package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class NotAction<CTX extends QuestContext> implements QuestAction<Boolean, CTX> {

    private final QuestAction<?, CTX> action;

    public NotAction(QuestAction<?, CTX> action) {
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return action.isAsync();
    }

    @Override
    public boolean isPersist() {
        return action.isPersist();
    }

    @Override
    public CompletableFuture<Boolean> process(CTX context) {
        return context.runAction("not", action).thenApplyAsync(
            t -> !Coerce.toBoolean(t),
            context.getExecutor()
        );
    }

    @Override
    public String getDataPrefix() {
        return "not";
    }

    @Override
    public String toString() {
        return "NotAction{" +
            "action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new NotAction<>(resolver.nextAction()),
            KetherCompleters.action(service)
        );
    }
}
