package io.izzel.kether.common.api;

import java.util.concurrent.CompletableFuture;

public abstract class QuestAction<T> {

    /**
     * This should not be called directly, see {@link QuestContext.Frame#newFrame(ParsedAction)}
     */
    public abstract CompletableFuture<T> process(QuestContext.Frame frame);

    public static <T> QuestAction<T> noop() {
        return new QuestAction<T>() {

            @Override
            public CompletableFuture<T> process(QuestContext.Frame frame) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String toString() {
                return "NoOp{}";
            }
        };
    }
}
