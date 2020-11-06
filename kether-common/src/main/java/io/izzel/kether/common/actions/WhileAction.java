package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class WhileAction implements QuestAction<Void> {

    private final QuestAction<?> condition;
    private final QuestAction<?> action;

    public WhileAction(QuestAction<?> condition, QuestAction<?> action) {
        this.condition = condition;
        this.action = action;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext context) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(context, future);
        return future;
    }

    private void process(QuestContext context, CompletableFuture<Void> future) {
        context.runAction(condition).thenAcceptAsync(t -> {
            if (Coerce.toBoolean(t)) {
                context.runAction(action).thenRunAsync(
                    () -> process(context, future),
                    context.getExecutor()
                );
            } else {
                future.complete(null);
            }
        }, context.getExecutor());
    }

    @Override
    public String toString() {
        return "WhileAction{" +
            "condition=" + condition +
            ", action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> {
                QuestAction<?> condition = resolver.nextAction();
                resolver.consume("then");
                QuestAction<?> action = resolver.nextAction();
                return new WhileAction(condition, action);
            },
            KetherCompleters.seq(
                KetherCompleters.action(service),
                KetherCompleters.constant("then"),
                KetherCompleters.action(service)
            )
        );
    }
}
