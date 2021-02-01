package io.izzel.kether.common.actions;

import io.izzel.kether.common.api.KetherCompleters;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestActionParser;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.data.ExitStatus;
import io.izzel.kether.common.loader.LoadError;
import io.izzel.kether.common.loader.types.ArgTypes;

import java.util.concurrent.CompletableFuture;

final class ExitAction extends QuestAction<Void> {

    private final boolean running;
    private final boolean waiting;
    private final long timeout;

    public ExitAction(boolean running, boolean waiting, long timeout) {
        this.running = running;
        this.waiting = waiting;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<Void> process(QuestContext.Frame frame) {
        long actual = timeout == 0 ? 0 : System.currentTimeMillis() + timeout;
        frame.context().setExitStatus(new ExitStatus(running, waiting, actual));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String toString() {
        return "ExitAction{" +
            "running=" + running +
            ", waiting=" + waiting +
            ", timeout=" + timeout +
            '}';
    }

    public static QuestActionParser parser() {
        return QuestActionParser.of(
            reader -> {
                String element = reader.nextToken();
                switch (element) {
                    case "success":
                        return new ExitAction(false, false, 0);
                    case "pause":
                        return new ExitAction(true, false, 0);
                    case "cooldown":
                        long l = reader.next(ArgTypes.DURATION).toMillis();
                        return new ExitAction(false, true, l);
                    default:
                        throw LoadError.NOT_MATCH.create("success|pause|cooldown", element);
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
