package io.izzel.kether.common.actions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.izzel.kether.common.api.*;
import io.izzel.kether.common.util.Coerce;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IfAction<U, CTX extends QuestContext> implements QuestAction<U, CTX> {

    private final QuestAction<?, CTX> condition;
    private final QuestAction<U, CTX> trueAction;
    private final QuestAction<U, CTX> falseAction;

    public IfAction(QuestAction<?, CTX> condition, QuestAction<U, CTX> trueAction, QuestAction<U, CTX> falseAction) {
        this.condition = condition;
        this.trueAction = trueAction;
        this.falseAction = falseAction;
    }

    @Override
    public boolean isAsync() {
        return condition.isAsync() || trueAction.isAsync();
    }

    @Override
    public CompletableFuture<U> process(QuestContext context) {
        if (isAsync()) {
            CompletableFuture<U> future = new CompletableFuture<>();
            context.runAction("condition", condition).thenAcceptAsync(t -> {
                if (Coerce.toBoolean(t)) {
                    context.runAction("trueAction", trueAction).thenAccept(future::complete);
                } else {
                    context.runAction("falseAction", falseAction).thenAccept(future::complete);
                }
            }, context.getExecutor());
            return future;
        } else {
            if (Coerce.toBoolean(context.runAction("condition", condition).join())) {
                return context.runAction("trueAction", trueAction);
            } else {
                return context.runAction("falseAction", falseAction);
            }
        }
    }

    @Override
    public String toString() {
        return "IfAction{" +
            "condition=" + condition +
            ", trueAction=" + trueAction +
            ", falseAction=" + falseAction +
            '}';
    }

    public static <U, CTX extends QuestContext> QuestActionParser parser(QuestService<CTX> service) {
        return QuestActionParser.<U, CTX>of(
            resolver -> {
                QuestAction<?, CTX> condition = resolver.nextAction();
                Preconditions.checkArgument(resolver.nextElement().equals("then"));
                QuestAction<U, CTX> trueAction = resolver.nextAction();
                if (resolver.hasNext()) {
                    resolver.mark();
                    String element = resolver.nextElement();
                    if (element.equals("else")) {
                        QuestAction<U, CTX> falseAction = resolver.nextAction();
                        return new IfAction<>(condition, trueAction, falseAction);
                    } else {
                        resolver.reset();
                    }
                }
                return new IfAction<>(condition, trueAction, QuestAction.noop());
            },
            KetherCompleters.seq(
                KetherCompleters.action(service),
                KetherCompleters.constant("then"),
                KetherCompleters.action(service),
                KetherCompleters.optional(
                    KetherCompleters.seq(
                        KetherCompleters.constant("else"),
                        KetherCompleters.action(service)
                    )
                )
            )
        );
    }
}
