package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.types.ArgTypes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AwaitAllAction extends QuestAction<Void> {

    private final List<ParsedAction<?>> actions;

    public AwaitAllAction(List<ParsedAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            ParsedAction<?> action = actions.get(i);
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
            resolver -> new AwaitAllAction(resolver.next(ArgTypes.listOf(ArgTypes.ACTION))),
            KetherCompleters.list(service)
        );
    }
}
