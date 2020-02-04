package io.izzel.kether.common.api;

import java.util.concurrent.CompletableFuture;

public interface QuestAction<T, CTX extends QuestContext> {

    boolean isAsync();

    CompletableFuture<T> process(CTX context);

    static <T, C extends QuestContext> QuestAction<T, C> noop() {
        return new QuestAction<T, C>() {
            @Override
            public boolean isAsync() {
                return false;
            }

            @Override
            public CompletableFuture<T> process(C context) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public String toString() {
                return "NoOp{}";
            }
        };
    }
}
