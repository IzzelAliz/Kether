package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class IfAction<U> extends QuestAction<U> {

    private final ParsedAction<?> condition;
    private final ParsedAction<U> trueAction;
    private final ParsedAction<U> falseAction;

    public IfAction(ParsedAction<?> condition, ParsedAction<U> trueAction, ParsedAction<U> falseAction) {
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
                ParsedAction<?> condition = resolver.nextAction();
                resolver.expect("then");
                ParsedAction<U> trueAction = resolver.nextAction();
                if (resolver.hasNext()) {
                    resolver.mark();
                    String element = resolver.nextToken();
                    if (element.equals("else")) {
                        ParsedAction<U> falseAction = resolver.nextAction();
                        return new IfAction<>(condition, trueAction, falseAction);
                    } else {
                        resolver.reset();
                    }
                }
                return new IfAction<>(condition, trueAction, ParsedAction.noop());
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
