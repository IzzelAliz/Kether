package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AwaitAllAction implements QuestAction<Void> {

    private final List<QuestAction<?>> actions;

    public AwaitAllAction(List<QuestAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext context) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            QuestAction<?> action = actions.get(i);
            CompletableFuture<?> future = context.runAction(action);
            futures[i] = future;
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String toString() {
        return "AwaitAllAction{" +
            "actions=" + actions +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AwaitAllAction(resolver.nextList()),
            KetherCompleters.list(service)
        );
    }
}
