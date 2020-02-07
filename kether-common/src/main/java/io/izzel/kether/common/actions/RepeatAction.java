package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.util.Coerce;

import java.util.concurrent.CompletableFuture;

final class RepeatAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final int time;
    private final QuestAction<?, CTX> action;

    public RepeatAction(int time, QuestAction<?, CTX> action) {
        this.time = time;
        this.action = action;
    }

    @Override
    public boolean isAsync() {
        return action.isAsync();
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        int cur = Coerce.toInteger(context.getTempData().getOrDefault("time", 0));
        if (isAsync()) {
            CompletableFuture<Void> future = new CompletableFuture<>();
            process(context, future, cur);
            return future;
        } else {
            for (int i = cur; i < time; i++) {
                context.runAction("repeat", action).join();
            }
            return CompletableFuture.completedFuture(null);
        }
    }

    private void process(CTX context, CompletableFuture<Void> future, int cur) {
        if (cur < time) {
            context.runAction("repeat", action).thenRunAsync(() -> {
                context.setTempData("time", cur + 1);
                process(context, future, cur + 1);
            }, context.getExecutor());
        } else {
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
            resolver -> new RepeatAction<>(resolver.nextInt(), resolver.nextAction()),
            KetherCompleters.seq(
                KetherCompleters.consume(),
                KetherCompleters.action(service)
            )
        );
    }
}
