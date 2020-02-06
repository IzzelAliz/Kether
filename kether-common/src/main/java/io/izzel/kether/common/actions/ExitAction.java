package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.ExitStatus;
import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.util.LocalizedException;

import java.util.concurrent.CompletableFuture;

final class ExitAction<CTX extends QuestContext> implements QuestAction<Void, CTX> {

    private final boolean running;
    private final boolean waiting;
    private final long timeout;

    public ExitAction(boolean running, boolean waiting, long timeout) {
        this.running = running;
        this.waiting = waiting;
        this.timeout = timeout;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public CompletableFuture<Void> process(CTX context) {
        long actual = timeout == 0 ? 0 : System.currentTimeMillis() + timeout;
        context.exit(new ExitStatus(running, waiting, actual));
        return CompletableFuture.completedFuture(null);
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            resolver -> {
                String element = resolver.nextElement();
                switch (element) {
                    case "success":
                        return new ExitAction<>(false, false, 0);
                    case "pause":
                        return new ExitAction<>(true, false, 0);
                    case "cooldown":
                        long l = resolver.nextLong();
                        return new ExitAction<>(false, true, l);
                    default:
                        throw LocalizedException.of("not-match", "success|pause|cooldown", element);
                }
            },
            KetherCompleters.firstParsing(
                KetherCompleters.constant("success"),
                KetherCompleters.constant("pause"),
                KetherCompleters.seq(
                    KetherCompleters.constant("cooldown"),
                    KetherCompleters.consume()
                )
            )
        );
    }
}
