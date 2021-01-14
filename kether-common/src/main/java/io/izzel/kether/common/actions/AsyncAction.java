package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.data.QuestFuture;
import io.izzel.kether.common.api.QuestService;

import java.util.concurrent.CompletableFuture;

public class AsyncAction<T> extends QuestAction<QuestFuture<T>> {

    private final QuestAction<T> action;

    public AsyncAction(QuestAction<T> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<QuestFuture<T>> process(QuestContext.Frame frame) {
        CompletableFuture<T> future = frame.newFrame(action).run();
        return CompletableFuture.completedFuture(new QuestFuture<>(action, future));
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AsyncAction<>(resolver.nextAction()),
            KetherCompleters.action(service)
        );
    }
}
