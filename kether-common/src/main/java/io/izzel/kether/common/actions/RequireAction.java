package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;
import io.izzel.kether.common.util.LocalizedException;

import java.util.concurrent.CompletableFuture;

final class RequireAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final QuestAction<?, CTX> condition;
    private final QuestAction<?, CTX> trigger;
    private final QuestAction<?, CTX> elseAction;

    public RequireAction(QuestAction<?, CTX> condition, QuestAction<?, CTX> trigger, QuestAction<?, CTX> elseAction) {
        if (!trigger.isAsync()) throw LocalizedException.of("require-async", trigger);
        this.condition = condition;
        this.trigger = trigger;
        this.elseAction = elseAction;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(context, future);
        return future;
    }

    private void process(CTX context, CompletableFuture<Void> future) {
        context.runAction("trigger", trigger).thenRunAsync(() ->
            context.runAction("condition", condition).thenAcceptAsync(t -> {
                if (Coerce.toBoolean(t)) {
                    future.complete(null);
                } else {
                    context.runAction("elseAction", elseAction).thenRunAsync(
                        () -> process(context, future),
                        context.getExecutor()
                    );
                }
            }, context.getExecutor()), context.getExecutor());
    }

    @Override
    public String toString() {
        return "RequireAction{" +
            "condition=" + condition +
            ", trigger=" + trigger +
            ", elseAction=" + elseAction +
            '}';
    }

    public static <CTX extends QuestContext> QuestActionParser parser(QuestService<CTX> service) {
        return QuestActionParser.<Void, CTX>of(
            resolver -> {
                QuestAction<?, CTX> condition = resolver.nextAction();
                resolver.consume("for");
                QuestAction<?, CTX> trigger = resolver.nextAction();
                if (resolver.hasNext()) {
                    resolver.mark();
                    String element = resolver.nextElement();
                    if (element.equals("else")) {
                        QuestAction<?, CTX> elseAction = resolver.nextAction();
                        return new RequireAction<>(condition, trigger, elseAction);
                    } else {
                        resolver.reset();
                    }
                }
                return new RequireAction<>(condition, trigger, QuestAction.noop());
            },
            KetherCompleters.seq(
                KetherCompleters.action(service),
                KetherCompleters.constant("for"),
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
