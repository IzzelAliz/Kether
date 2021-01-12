package io.izzel.kether.common.api;

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

    interface Frame {

        String name();

        QuestContext context();

        Optional<QuestAction<?>> currentAction();

        List<Frame> children();

        default Stream<Frame> walkFrames() {
            return walkFrames(Integer.MAX_VALUE);
        }

        default Stream<Frame> walkFrames(int depth) {
            return this.children().stream()
                .map(it -> it.walkFrames(depth - 1))
                .reduce(Stream.of(this), Stream::concat);
        }

        Optional<Frame> parent();

        void setNext(QuestAction<?> action);

        void setNext(Quest.Block block);

        Frame newFrame(String name);

        Frame newFrame(QuestAction<?> action);

        VarTable variables();

        /**
         * The closable will called immediately when action is complete
         *
         * @param closeable resources to clean
         */
        <T extends AutoCloseable> T addClosable(T closeable);

        <T> CompletableFuture<T> run();

        void close();
    }

    interface VarTable {

        <T> Optional<T> get(String name) throws CompletionException;

        <T> Optional<QuestFuture<T>> getFuture(String name);

        void set(String name, Object value);

        <T> void set(String name, QuestAction<T> owner, CompletableFuture<T> future);

        Set<String> keys();

        Collection<Map.Entry<String, Object>> values();

        void initialize(Frame frame);

        void close();
    }
}
