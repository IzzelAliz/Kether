package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.*;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.loader.types.ArgTypes;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class NotAction extends QuestAction<Boolean> {

    private final ParsedAction<?> action;

    public NotAction(ParsedAction<?> action) {
        this.action = action;
    }

    @Override
    public CompletableFuture<Boolean> process(QuestContext.Frame frame) {
        return frame.newFrame(action).run().thenApply(t -> !Coerce.toBoolean(t));
    }

    @Override
    public String toString() {
        return "NotAction{" +
            "action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new NotAction(resolver.next(ArgTypes.ACTION)),
            KetherCompleters.action(service)
        );
    }
}
