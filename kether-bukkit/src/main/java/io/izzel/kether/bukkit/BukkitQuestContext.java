package io.izzel.kether.bukkit;

import io.izzel.kether.common.api.AbstractQuestContext;
import io.izzel.kether.common.api.PersistentQuestContext;
import io.izzel.kether.common.api.Quest;
import io.izzel.kether.common.api.QuestContext;
import io.izzel.kether.common.api.QuestService;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.Executor;

public class BukkitQuestContext extends PersistentQuestContext {

    protected BukkitQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey) {
        super(service, parent, quest, playerIdentifier, runningBlock, index, dataKey, tempData, persistentData, childKey);
    }

    public Player getPlayer() {
        return KetherPlugin.instance().getKetherConfig().getIdentifierProvider().get(this.getPlayerIdentifier()).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    protected Executor createExecutor() {
        return new BukkitSchedulerExecutor();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends QuestContext> C createChild(String key) {
        return (C) new BukkitQuestContext(getService(), this, getQuest(), getPlayerIdentifier(), getRunningBlock(), getIndex(), getDataKey(), null, getPersistentData(), key);
    }
}
