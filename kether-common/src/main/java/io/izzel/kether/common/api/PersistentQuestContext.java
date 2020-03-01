package io.izzel.kether.common.api;

import io.izzel.kether.common.actions.DataAction;
import io.izzel.kether.common.util.Coerce;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class PersistentQuestContext extends AbstractQuestContext {

    private boolean doRecover = true;
    private int dataOffset = 0;

    protected PersistentQuestContext(QuestService<?> service, AbstractQuestContext parent, Quest quest, String playerIdentifier, String runningBlock, int index, String dataKey, Map<String, Object> tempData, Map<String, Object> persistentData, String childKey, boolean anonymous) {
        super(service, parent, quest, playerIdentifier, runningBlock, index, dataKey, tempData, persistentData, childKey, anonymous);
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Void> runActions() {
        if (doRecover) {
            Object o = this.getTempData().get("$");
            if (o instanceof Map) {
                Map<String, Object> map = ((Map<String, Object>) o);
                if (!map.isEmpty()) {
                    this.setIndex(Coerce.asInteger(map.get("index")).orElse(this.getIndex()));
                    this.setRunningBlock(Coerce.asString(map.get("block")).orElse(this.getRunningBlock()));
                    String storedKey = Coerce.asString(map.get("dataKey")).orElse(this.getDataKey());
                    int storedOffset = Coerce.asInteger(map.get("dataOffset")).orElse(0);
                    Object prefix = map.get("action");
                    Quest.Block block = this.getQuest().getBlock(this.getRunningBlock()).orElseThrow(NullPointerException::new);
                    if (!this.tryRecover(storedKey, Coerce.asString(prefix).orElse(null), block, storedOffset, this.getIndex())) {
                        throw new IllegalArgumentException("Cannot recover");
                    }
                }
            }
            doRecover = false;
        }
        return super.runActions();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void nextAction() {
        super.nextAction();
        this.dataOffset++;
        Map<String, Object> map = (Map<String, Object>) this.tempData.computeIfAbsent("$", k -> new HashMap<>());
        map.put("block", this.getRunningBlock());
        map.put("index", this.getIndex());
        map.put("dataKey", this.getDataKey());
        map.put("dataOffset", this.dataOffset);
        try {
            QuestAction<?, ?> action = this.currentAction();
            if (action.isPersist()) {
                map.put("action", action.getDataPrefix());
            } else {
                map.remove("action");
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void setDataKey(String key) {
        super.setDataKey(key);
        this.dataOffset = 0;
    }

    private boolean tryRecover(String storedKey, String prefix, Quest.Block block, int offset, int index) {
        String dataKey = BASE_DATA_KEY;
        boolean keyMatch = dataKey.equals(storedKey);
        boolean offsetMatch = false;
        boolean indexMatch = false;
        int dataOffset = 0;
        for (int i = 0; i < block.getActions().size(); i++) {
            QuestAction<?, QuestContext> questAction = block.getActions().get(i);
            if (questAction instanceof DataAction) {
                dataKey = ((DataAction<?>) questAction).getKey();
                keyMatch = dataKey.equals(storedKey);
                dataOffset = 0;
            }
            if (keyMatch && questAction.getDataPrefix().equals(prefix)) {
                this.setIndex(i);
                this.setDataKey(dataKey);
                this.dataOffset = dataOffset;
                return true;
            }
            if (keyMatch && dataOffset == offset) {
                this.setIndex(i);
                this.setDataKey(dataKey);
                this.dataOffset = dataOffset;
                offsetMatch = true;
            }
            if (!offsetMatch && keyMatch && i == index) {
                this.setIndex(i);
                this.setDataKey(dataKey);
                this.dataOffset = dataOffset;
                indexMatch = true;
            }
            dataOffset++;
        }
        return offsetMatch || indexMatch;
    }

    private QuestAction<?, ?> currentAction() {
        return this.getQuest().getBlock(this.getRunningBlock()).orElseThrow(NullPointerException::new)
            .getActions().get(this.getIndex());
    }
}
