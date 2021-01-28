package io.izzel.kether.common.api;

import io.izzel.kether.common.api.data.ExitStatus;
import io.izzel.kether.common.api.data.QuestFuture;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public interface QuestContext {

    String BASE_BLOCK = "main";

    QuestService<? extends QuestContext> getService();

    Quest getQuest();

    String getPlayerIdentifier();

    void setExitStatus(ExitStatus exitStatus);

    Optional<ExitStatus> getExitStatus();

    CompletableFuture<Object> runActions();

    Executor getExecutor();

    void terminate();

    Frame rootFrame();

    interface Frame extends AutoCloseable {

        String name();

        QuestContext context();

        Optional<ParsedAction<?>> currentAction();

        List<Frame> children();

        default Stream<Frame> walkFrames() {
            return walkFrames(Integer.MAX_VALUE);
        }

        default Stream<Frame> walkFrames(int depth) {
            if (depth < 0) return Stream.empty();
            else if (depth == 0) return Stream.of(this);
            else return this.children().stream()
                    .map(it -> it.walkFrames(depth - 1))
                    .reduce(Stream.of(this), Stream::concat);
        }

        Optional<Frame> parent();

        void setNext(ParsedAction<?> action);

        void setNext(Quest.Block block);

        Frame newFrame(String name);

        Frame newFrame(ParsedAction<?> action);

        VarTable variables();

        /**
         * The closable will called immediately when action is complete
         *
         * @param closeable resources to clean
         */
        <T extends AutoCloseable> T addClosable(T closeable);

        <T> CompletableFuture<T> run();

        void close();

        boolean isDone();
    }

    interface VarTable {

        <T> Optional<T> get(String name) throws CompletionException;

        <T> Optional<QuestFuture<T>> getFuture(String name);

        void set(String name, Object value);

        <T> void set(String name, ParsedAction<T> owner, CompletableFuture<T> future);

        Set<String> keys();

        Collection<Map.Entry<String, Object>> values();

        void initialize(Frame frame);

        void close();
    }
}
