package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AllAction<CTX extends QuestContext> implements QuestAction<Boolean, CTX> {

    private final List<QuestAction<?, CTX>> actions;

    public AllAction(List<QuestAction<?, CTX>> actions) {
        this.actions = actions;
    }

    @Override
    public boolean isAsync() {
        return actions.stream().anyMatch(QuestAction::isAsync);
    }

    @Override
    public boolean isPersist() {
        return actions.stream().anyMatch(QuestAction::isPersist);
    }

    @Override
    public CompletableFuture<Boolean> process(CTX context) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
        for (int i = 0; i < actions.size(); i++) {
            QuestAction<?, CTX> action = actions.get(i);
            CompletableFuture<?> f = context.runAction(String.valueOf(i), action);
            future = future.thenCombine(f, (b, o) -> b && Coerce.toBoolean(o));
        }
        return future;
    }

    @Override
    public String getDataPrefix() {
        return "all";
    }

    @Override
    public String toString() {
        return "AllAction{" +
            "actions=" + actions +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AllAction<>(resolver.nextList()),
            KetherCompleters.list(service)
        );
    }
}
