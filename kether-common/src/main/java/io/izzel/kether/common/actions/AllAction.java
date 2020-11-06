package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AllAction implements QuestAction<Boolean> {

    private final List<QuestAction<?>> actions;

    public AllAction(List<QuestAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Boolean> process(QuestContext context) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        for (QuestAction<?> action : actions) {
            CompletableFuture<?> f = context.runAction(action);
            future = future.thenCombine(f, (b, o) -> b && Coerce.toBoolean(o));
        }
        return future;
    }

    @Override
    public String toString() {
        return "AllAction{" +
            "actions=" + actions +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AllAction(resolver.nextList()),
            KetherCompleters.list(service)
        );
    }
}
