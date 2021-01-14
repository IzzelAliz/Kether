package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class IfAction<U> extends QuestAction<U> {

    private final QuestAction<?> condition;
    private final QuestAction<U> trueAction;
    private final QuestAction<U> falseAction;

    public IfAction(QuestAction<?> condition, QuestAction<U> trueAction, QuestAction<U> falseAction) {
        this.condition = condition;
        this.trueAction = trueAction;
        this.falseAction = falseAction;
    }

    @Override
    public CompletableFuture<U> process(QuestContext.Frame frame) {
        CompletableFuture<U> future = new CompletableFuture<>();
        frame.newFrame(condition).run().thenAccept(t -> {
            if (Coerce.toBoolean(t)) {
                frame.newFrame(trueAction).<U>run().thenAccept(future::complete);
            } else {
                frame.newFrame(falseAction).<U>run().thenAccept(future::complete);
            }
        });
        return future;
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
        return QuestActionParser.of(
            resolver -> {
                QuestAction<?> condition = resolver.nextAction();
                resolver.expect("then");
                QuestAction<U> trueAction = resolver.nextAction();
                if (resolver.hasNext()) {
                    resolver.mark();
                    String element = resolver.nextToken();
                    if (element.equals("else")) {
                        QuestAction<U> falseAction = resolver.nextAction();
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
