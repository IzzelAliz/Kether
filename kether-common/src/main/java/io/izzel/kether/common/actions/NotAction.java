package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class NotAction extends QuestAction<Boolean> {

    private final QuestAction<?> action;

    public NotAction(QuestAction<?> action) {
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
            resolver -> new NotAction(resolver.nextAction()),
            KetherCompleters.action(service)
        );
    }
}
