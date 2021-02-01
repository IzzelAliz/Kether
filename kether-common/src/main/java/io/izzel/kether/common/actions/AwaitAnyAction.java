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

final class AwaitAnyAction extends QuestAction<Object> {

    private final List<ParsedAction<?>> actions;

    public AwaitAnyAction(List<ParsedAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Object> process(QuestContext.Frame frame) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            ParsedAction<?> action = actions.get(i);
            futures[i] = frame.newFrame(action).run();
        }
        return CompletableFuture.anyOf(futures);
    }

    @Override
    public String toString() {
        return "AwaitAnyAction{" +
            "actions=" + actions +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AwaitAnyAction(resolver.next(ArgTypes.listOf(ArgTypes.ACTION))),
            KetherCompleters.list(service)
        );
    }
}
