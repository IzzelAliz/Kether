package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class WhileAction extends QuestAction<Void> {

    private final ParsedAction<?> condition;
    private final ParsedAction<?> action;

    public WhileAction(ParsedAction<?> condition, ParsedAction<?> action) {
        this.condition = condition;
        this.action = action;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(frame, future);
        return future;
    }

    private void process(QuestContext.Frame frame, CompletableFuture<Void> future) {
        frame.newFrame(action).run().thenAcceptAsync(t -> {
            if (Coerce.toBoolean(t)) {
                frame.newFrame(action).run().thenRunAsync(
                    () -> process(frame, future),
                    frame.context().getExecutor()
                );
            } else {
                future.complete(null);
            }
        }, frame.context().getExecutor());
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
                ParsedAction<?> condition = resolver.nextAction();
                resolver.expect("then");
                ParsedAction<?> action = resolver.nextAction();
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
