package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.loader.types.ArgTypes;
import io.izzel.kether.common.util.Coerce;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AnyAction extends QuestAction<Boolean> {

    private final List<ParsedAction<?>> actions;

    public AnyAction(List<ParsedAction<?>> actions) {
        this.actions = actions;
    }

    @Override
    public CompletableFuture<Boolean> process(QuestContext.Frame frame) {
        CompletableFuture<Boolean> future = CompletableFuture.completedFuture(false);
        for (ParsedAction<?> action : actions) {
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
            resolver -> new AnyAction(resolver.next(ArgTypes.listOf(ArgTypes.ACTION))),
            KetherCompleters.list(service)
        );
    }
}
