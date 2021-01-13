package io.izzel.kether.common.api.data;

import com.google.common.base.Preconditions;
import io.izzel.kether.common.api.QuestAction;
import io.izzel.kether.common.api.QuestContext;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class QuestFuture<T> {

    private final QuestAction<T> action;
    private CompletableFuture<T> future;

    public QuestFuture(QuestAction<T> action) {
        this(action, null);
    }

    public QuestFuture(QuestAction<T> action, CompletableFuture<T> future) {
        this.action = action;
        this.future = future;
    }

    public QuestAction<T> getAction() {
        return action;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public void run(QuestContext.Frame frame) {
        Preconditions.checkState(this.future == null, "already running");
        this.future = frame.newFrame(this.action).run();
    }

    public void close() {
        Preconditions.checkState(this.future != null, "not running");
        this.future = null;
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
