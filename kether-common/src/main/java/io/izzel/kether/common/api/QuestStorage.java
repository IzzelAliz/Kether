package io.izzel.kether.common.api;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface QuestStorage {

    void init() throws Exception;

    void close() throws Exception;

    CompletableFuture<Collection<QuestContext>> fetchContexts(String playerIdentifier);

    CompletableFuture<Optional<QuestContext>> fetchContext(String playerIdentifier, String questId);

    CompletableFuture<Void> updateContext(String playerIdentifier, QuestContext context);

    void discardContext(String playerIdentifier);

}
