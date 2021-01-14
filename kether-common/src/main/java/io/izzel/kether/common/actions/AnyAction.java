package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AnyAction extends QuestAction<Boolean> {

    private final List<QuestAction<?>> actions;

    public AnyAction(List<QuestAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Boolean> process(QuestContext.Frame frame) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(false);
        for (QuestAction<?> action : actions) {
            CompletableFuture<?> f = frame.newFrame(action).run();
            future = future.thenCombine(f, (b, o) -> b || Coerce.toBoolean(o));
        }
        return future;
    }

    @Override
    public String toString() {
        return "AnyAction{" +
            "actions=" + actions +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new AnyAction(resolver.nextList()),
            KetherCompleters.list(service)
        );
    }
}
