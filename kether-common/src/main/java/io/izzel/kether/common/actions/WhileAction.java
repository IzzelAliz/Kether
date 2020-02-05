package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class WhileAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final QuestAction<?, CTX> condition;
    private final QuestAction<?, CTX> action;

    public WhileAction(QuestAction<?, CTX> condition, QuestAction<?, CTX> action) {
        this.condition = condition;
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return condition.isAsync() || action.isAsync();
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        if (isAsync()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            process(context, future);
            return future;
        } else {
            while (Coerce.toBoolean(context.runAction("condition", condition).join())) {
                context.runAction("action", action).join();
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private void process(CTX context, CompletableFuture<Void> future) {
        context.runAction("condition", condition).thenAcceptAsync(t -> {
            if (Coerce.toBoolean(t)) {
                context.runAction("action", action).thenRunAsync(
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

    public static <CTX extends QuestContext> QuestActionParser parser(QuestService<CTX> service) {
        return QuestActionParser.<Void, CTX>of(
            resolver -> {
                QuestAction<?, CTX> condition = resolver.nextAction();
                resolver.consume("then");
                QuestAction<?, CTX> action = resolver.nextAction();
                return new WhileAction<>(condition, action);
            },
            KetherCompleters.seq(
                KetherCompleters.action(service),
                KetherCompleters.constant("then"),
                KetherCompleters.action(service)
            )
        );
    }
}
