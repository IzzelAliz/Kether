package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
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
            frame.newFrame(action).thenRunAsync(() -> {
                context.putLocal("times", cur + 1);
                process(context, future, cur + 1);
            }, context.getExecutor());
        } else {
            context.putLocal("times", null);
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
