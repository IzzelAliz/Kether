package io.izzel.kether.common.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface QuestContext {

    String BASE_BLOCK = "main";

    String BASE_DATA_KEY = "default";

    Quest getQuest();

    String getPlayerIdentifier();

    String getRunningBlock();

    int getIndex();

    void setExitStatus(ExitStatus exitStatus);

    Optional<ExitStatus> getExitStatus();

    void setJump(String block, int index);

    <C extends QuestContext> C createChild(String key, boolean anonymous);

    CompletableFuture<Void> runActions();

    Executor getExecutor();

    void setDataKey(String key);

    String getDataKey();

    <T, C extends QuestContext> CompletableFuture<T> runAction(String key, QuestAction<T, C> action);

    /**
     * Return a mutable map storing temp data.
     * <p>
     * When the action completes its process, all data stored in will be removed.
     *
     * @return a mutable map storing temp data
     */
    Map<String, Object> getTempData();

    default void setTempData(String key, Object value) {
        getTempData().put(key, value);
    }

    Map<String, Object> getPersistentData();

    /**
     * The closable will called immediately when action is complete, and(or)
     * quest is cancelled(player is offline, etc).
     *
     * @param closeable resources to clean when action complete.
     */
    <T extends AutoCloseable> T addClosable(T closeable);

    void terminate();

    boolean isDirty();
}
