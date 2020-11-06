package io.izzel.kether.common.api;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class QuestFuture<T> {

    private final QuestAction<T> action;
    private final int address;
    private final CompletableFuture<T> future;

    public QuestFuture(QuestAction<T> action, int address, CompletableFuture<T> future) {
        this.action = action;
        this.address = address;
        this.future = future;
    }

    public QuestAction<T> getAction() {
        return action;
    }

    public int getAddress() {
        return address;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public static <T> QuestFuture<T> of(QuestContext context, QuestAction<T> action, CompletableFuture<T> future) {
        int address = context.getBlockRunning().getAddress(action);
        return new QuestFuture<>(action, address, future);
    }

    @SuppressWarnings("unchecked")
    public static <T> Consumer<T> complete(CompletableFuture<T> future) {
        return it -> {
            if (it instanceof QuestFuture) {
                ((QuestFuture<T>) it).getFuture().thenAccept(future::complete);
            } else {
                future.complete(it);
            }
        };
    }
}
