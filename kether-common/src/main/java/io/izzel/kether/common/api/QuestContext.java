package io.izzel.kether.common.api;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface QuestContext {

    String BASE_BLOCK = "main";

    QuestService<? extends QuestContext> getService();

    Quest getQuest();

    String getPlayerIdentifier();

    Quest.Block getBlockRunning();

    void setBlock(Quest.Block block);

    void setExitStatus(ExitStatus exitStatus);

    Optional<ExitStatus> getExitStatus();

    QuestContext createChild();

    CompletableFuture<Object> runActions();

    Executor getExecutor();

    <T> CompletableFuture<T> runAction(QuestAction<T> action);

    Map<String, Object> locals();

    void putLocal(String key, Object value);

    <T> T getLocal(String key);

    /**
     * The closable will called immediately when action is complete, and(or)
     * quest is cancelled(player is offline, etc).
     *
     * @param closeable resources to clean when action complete.
     */
    <T extends AutoCloseable> T addClosable(T closeable);

    void terminate();
}
