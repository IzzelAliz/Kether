package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestFuture;
import io.izzel.kether.common.api.QuestService;

import java.util.concurrent.CompletableFuture;

public class AsyncAction<T> implements QuestAction<QuestFuture<T>> {

    private final QuestAction<T> action;

    public AsyncAction(QuestAction<T> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<QuestFuture<T>> process(QuestContext context) {
        QuestContext child = context.createChild();
        CompletableFuture<T> future = child.runAction(action);
        return CompletableFuture.completedFuture(QuestFuture.of(child, action, future));
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AsyncAction<>(resolver.nextAction()),
            KetherCompleters.action(service)
        );
    }
}
