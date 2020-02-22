package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class AwaitAllAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final List<QuestAction<?, CTX>> actions;

    public AwaitAllAction(List<QuestAction<?, CTX>> actions) {
        this.actions = actions;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        CompletableFuture<?>[] futures = new CompletableFuture[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            QuestAction<?, CTX> action = actions.get(i);
            CompletableFuture<?> future = context.runAction(String.valueOf(i), action);
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
            resolver -> new AwaitAllAction<>(resolver.nextList()),
            KetherCompleters.seq(
                KetherCompleters.firstParsing(
                    KetherCompleters.constant("["),
                    KetherCompleters.constant("begin")
                ),
                KetherCompleters.some(
                    KetherCompleters.action(service)
                ),
                KetherCompleters.firstParsing(
                    KetherCompleters.constant("]"),
                    KetherCompleters.constant("end")
                )
            )
        );
    }
}
