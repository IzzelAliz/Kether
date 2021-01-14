package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.persistent.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class RepeatAction extends QuestAction<Void> {

    private final int time;
    private final QuestAction<?> action;

    public RepeatAction(int time, QuestAction<?> action) {
        this.time = time;
        this.action = action;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        int cur = Coerce.toInteger(frame.variables().get("times").orElse(0));
        CompletableFuture<Void> future = new CompletableFuture<>();
        process(frame, future, cur);
        return future;
    }

    private void process(QuestContext.Frame frame, CompletableFuture<Void> future, int cur) {
        if (cur < time) {
            frame.newFrame(action).run().thenRunAsync(() -> {
                frame.variables().set("times", cur + 1);
                process(frame, future, cur + 1);
            }, frame.context().getExecutor());
        } else {
            frame.variables().set("times", null);
            future.complete(null);
        }
    }

    @Override
    public String toString() {
        return "RepeatAction{" +
            "time=" + time +
            ", action=" + action +
            '}';
    }

    public static QuestActionParser parser(QuestService<?> service) {
        return QuestActionParser.of(
            resolver -> new RepeatAction(resolver.nextInt(), resolver.nextAction()),
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.action(service)
            )
        );
    }
}
