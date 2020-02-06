package io.izzel.kether.common.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface QuestStorage {

    CompletableFuture<List<QuestContext>> fetchContexts(String playerIdentifier);

    CompletableFuture<Void> updateContext(String playerIdentifier, QuestContext context);

}
