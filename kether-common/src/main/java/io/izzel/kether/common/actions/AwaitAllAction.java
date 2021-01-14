package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AwaitAllAction extends QuestAction<Void> {

    private final List<QuestAction<?>> actions;

    public AwaitAllAction(List<QuestAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            QuestAction<?> action = actions.get(i);
            futures[i] = frame.newFrame(action).run();
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
