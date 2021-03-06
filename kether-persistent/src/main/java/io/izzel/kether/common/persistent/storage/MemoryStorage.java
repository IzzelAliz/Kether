package io.izzel.kether.common.persistent.storage;

import io.izzel.kether.common.api.ParsedAction;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.persistent.QuestStorage;
import io.izzel.kether.common.persistent.QuestTableStore;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage implements QuestStorage {

    private final Map<String, Map<String, QuestContext>> map = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public void close() {
    }

    @Override
    public CompletableFuture<Collection<QuestContext>> fetchContexts(String playerIdentifier) {
        Map<String, QuestContext> playerMap = map.computeIfAbsent(playerIdentifier, k -> new ConcurrentHashMap<>());
        return CompletableFuture.completedFuture(playerMap.values());
    }

    @Override
    public CompletableFuture<Optional<QuestContext>> fetchContext(String playerIdentifier, String questId) {
        Map<String, QuestContext> playerMap = map.computeIfAbsent(playerIdentifier, k -> new ConcurrentHashMap<>());
        return CompletableFuture.completedFuture(Optional.ofNullable(playerMap.get(questId)));
    }

    @Override
    public CompletableFuture<Void> updateContext(String playerIdentifier, QuestContext context) {
        Map<String, QuestContext> playerMap = map.computeIfAbsent(playerIdentifier, k -> new ConcurrentHashMap<>());
        playerMap.put(context.getQuest().getId(), context);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void discardContext(String playerIdentifier) {
        map.remove(playerIdentifier);
    }

    @Override
    public QuestTableStore getTableStore() {
        return id -> block -> new QuestTableStore.QuestTable.BlockTable() {
            @Override
            public void pushAction() {
            }

            @Override
            public void transferTo(ParsedAction<?> before, ParsedAction<?> after) {
            }

            @Override
            public void drop(ParsedAction<?> action) {
            }

            @Override
            public void insert(ParsedAction<?> action) {
            }

            @Override
            public void assign(ParsedAction<?> action) {
            }
        };
    }
}
