package io.izzel.kether.common.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface QuestContext {

    Quest getQuest();

    String getRunningBlock();

    int getIndex();

    /**
     * Advance the index, or jump to specific location, then return next possible action,
     * or return empty if
     * <p>
     * (1) the index reach the end, or
     * <p>
     * (2) the {@link #exit} method is called.
     *
     * @return optional next action
     */
    <C extends QuestContext> Optional<QuestAction<?, C>> nextAction();

    void exit();

    void setJump(String block, int index);

    QuestContext forkChild(String key);

    CompletableFuture<Void> runActions();

    Executor getExecutor();

    void setDataStore(String key);

    String getDataStore();

    <T, C extends QuestContext> CompletableFuture<T> runAction(String key, QuestAction<T, C> action);

    /**
     * Return a mutable map storing persistent data.
     * <p>
     * When the action completes its process, all data stored in will be removed.
     *
     * @return a mutable map storing persistent data
     */
    Map<String, Object> getPersistentData();

    @SuppressWarnings("unchecked")
    default <T> T getPersistentData(String key) {
        return (T) getPersistentData().get(key);
    }

    default void setPersistentData(String key, Object value) {
        getPersistentData().put(key, value);
    }

    /**
     * The closable will called immediately when action is complete, and(or)
     * quest is cancelled(player is offline, etc).
     *
     * @param closeable resources to clean when action complete.
     */
    void addClosable(AutoCloseable closeable);
}
