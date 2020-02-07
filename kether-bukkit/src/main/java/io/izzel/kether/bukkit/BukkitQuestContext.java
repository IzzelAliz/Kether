package io.izzel.kether.bukkit;

import io.izzel.kether.common.api.AbstractQuestContext;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class BukkitQuestContext extends AbstractQuestContext {

    protected BukkitQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey) {
        super(service, parent, quest, playerIdentifier, runningBlock, index, dataKey, tempData, persistentData, childKey);
    }

    @Override
    protected Executor createExecutor() {
        return new BukkitSchedulerExecutor();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends QuestContext> C createChild(String key) {
        Object o = getTempData().computeIfAbsent(key, k -> new HashMap<>());
        if (o instanceof Map) {
            return (C) new BukkitQuestContext(getService(), this, getQuest(), getPlayerIdentifier(), getRunningBlock(), getIndex(), getDataKey(), ((Map<String, Object>) o), getPersistentData(), key);
        } else throw new IllegalArgumentException(key);
    }
}
