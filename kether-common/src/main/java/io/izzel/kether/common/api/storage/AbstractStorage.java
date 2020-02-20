package io.izzel.kether.common.api.storage;

import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import io.izzel.kether.common.api.QuestStorage;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractStorage implements QuestStorage {

    protected final QuestService<?> service;
    protected final Map<String, Map<String, QuestContext>> map = new HashMap<>();
    protected Yaml yaml;

    protected AbstractStorage(QuestService<?> service) {
        this.service = service;
    }

    @Override
    public CompletableFuture<Collection<QuestContext>> fetchContexts(String playerIdentifier) {
        if (map.containsKey(playerIdentifier)) {
            return CompletableFuture.completedFuture(Collections.unmodifiableCollection(map.get(playerIdentifier).values()));
        } else {
            return CompletableFuture.supplyAsync(
                () -> this.supplyAll(playerIdentifier),
                service.getAsyncExecutor()
            ).thenApplyAsync(
                contexts -> this.convertAll(contexts, playerIdentifier),
                service.getExecutor()
            );
        }
    }

    @Override
    public CompletableFuture<Optional<QuestContext>> fetchContext(String playerIdentifier, String questId) {
        if (!this.map.containsKey(playerIdentifier)) {
            return fetchContexts(playerIdentifier).thenApply(col -> Optional.ofNullable(this.map.get(playerIdentifier).get(questId)));
        } else {
            return CompletableFuture.completedFuture(Optional.ofNullable(this.map.get(playerIdentifier).get(questId)));
        }
    }

    @Override
    public void discardContext(String playerIdentifier) {
        this.map.remove(playerIdentifier);
    }

    protected abstract Collection<String> supplyAll(String playerIdentifier);

    protected Collection<QuestContext> convertAll(Collection<String> contexts, String playerIdentifier) {
        ArrayList<QuestContext> list = new ArrayList<>();
        for (String context : contexts) {
            Object load = this.yaml.load(context);
            if (load instanceof QuestContext) list.add(((QuestContext) load));
        }
        Map<String, QuestContext> map = this.map.computeIfAbsent(playerIdentifier, k -> new HashMap<>());
        for (QuestContext context : list) {
            map.put(context.getQuest().getId(), context);
        }
        return list;
    }
}
